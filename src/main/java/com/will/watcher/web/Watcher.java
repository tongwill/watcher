package com.will.watcher.web;

import com.will.watcher.handlebar.Handlebar;
import com.will.watcher.handler.AjaxHandler;
import com.will.watcher.handler.AssetsHandler;
import com.will.watcher.handler.DubboHandler;
import com.will.watcher.handler.ListenerHandler;
import com.will.watcher.util.CommonUtil;
import com.will.watcher.util.Setting;
import io.terminus.pampas.common.BaseUser;
import io.terminus.pampas.common.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class Watcher {
    private static final Logger LOG = LoggerFactory.getLogger(Watcher.class);

    @Autowired
    private Handlebar handlebar;

    @Autowired
    private AssetsHandler assetsHandler;

    @Autowired
    private AjaxHandler ajaxHandler;

    @Autowired
    private ListenerHandler listenerHandler;

    @Autowired
    private DubboHandler dubboHandler;

    @Autowired
    private BaseUser baseUser;

    @Autowired
    private Setting setting;

    @RequestMapping
    public void doRequest(@RequestHeader("Host") String host, HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        try {
            String path = request.getRequestURI().substring(request.getContextPath().length() + 1);
            context.put("_PATH_",path);
            response.setCharacterEncoding("UTF-8");
            String uri = ((HttpServletRequest) request).getServletPath();
            context = prepareContext(request, context);
            //监听页面
            boolean isListener=listenerHandler.handle(path,response,context);
            if (isListener) return;
            //dubbotest
            boolean isDubboTest=this.dubboHandler.handle(path,response,context);
            if (isDubboTest) return;
            //静态资源
            boolean isAssert = this.assetsHandler.handle(path, response);
            if (isAssert) return;
            //是否显示条件
            if(setting.getShowRequest()){
                String query=CommonUtil.getQueryFromContext(context);
                if (query.length()!=0){
                    LOG.info("request:{}",query);
                }
            }
            //ajax请求
            boolean isAjax = this.ajaxHandler.handle(path, response,context);
            if (isAjax) return;
            //handlebars模版
            String html=handlebar.execPath(uri, context, false);
            if(html.length()!=0){
                response.getWriter().write(html);
            }else{
                response.getWriter().write("no result");
            }
        }catch(Exception e){
            LOG.error("watcher error,{}",e.toString());
        }
    }

    private Map<String, Object> prepareContext(HttpServletRequest request, Map<String, Object> context) {
        if (request != null) {
            for (Object name : request.getParameterMap().keySet()) {
                context.put((String) name, request.getParameter((String) name));
            }
        }
        //登录信息注入
        UserUtil.putCurrentUser(baseUser);
        context.put("_USER_", baseUser);
        return context;
    }
}