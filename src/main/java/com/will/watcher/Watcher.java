package com.will.watcher;

import com.google.common.base.Throwables;
import com.will.watcher.dubbo.DubboHandler;
import com.will.watcher.dubbo.ListenerHandler;
import com.will.watcher.handlebar.AjaxHandler;
import com.will.watcher.handlebar.Handlebar;
import com.will.watcher.handlebar.RefreshHandler;
import io.terminus.common.utils.Splitters;
import io.terminus.pampas.common.BaseUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.model.App;
import io.terminus.pampas.webc.controller.AssetsHandler;
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
    private RefreshHandler refreshHandler;

    @Autowired
    private App app;

    @RequestMapping
    public void doRequest(@RequestHeader("Host") String host, HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        try {
            String domain = (String) Splitters.COLON.splitToList(host).get(0);
            String path = request.getRequestURI().substring(request.getContextPath().length() + 1);
            context.put("_PATH_",path);
            response.setCharacterEncoding("UTF-8");
            String uri = ((HttpServletRequest) request).getServletPath();
            context = prepareContext(request, context);
            //登录信息注入
            UserUtil.putCurrentUser(baseUser);
            //监听页面
            boolean isListener=listenerHandler.handle(path,response,context);
            if (isListener) return;
            //刷新页面
            boolean isRefresh=refreshHandler.handle(path,response,context);
            if (isRefresh) return;
            //dubbotest
            boolean isDubboTest=this.dubboHandler.handle(path,response,context);
            if (isDubboTest) return;
            //静态资源
            boolean isAssert = this.assetsHandler.handle(path, response);
            if (isAssert) return;
            //ajax请求
            boolean isAjax = this.ajaxHandler.handle(path, response,context);
            if (isAjax) return;
            //handlebars模版
            response.getWriter().write(handlebar.execPath(uri, context, false));
        }catch(Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    private Map<String, Object> prepareContext(HttpServletRequest request, Map<String, Object> context) {
        if (request != null) {
            for (Object name : request.getParameterMap().keySet()) {
                context.put((String) name, request.getParameter((String) name));
            }
        }
        context.put("_USER_", UserUtil.getCurrentUser());
        return context;
    }
}