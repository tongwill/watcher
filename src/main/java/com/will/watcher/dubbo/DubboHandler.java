package com.will.watcher.dubbo;

import com.google.common.base.Throwables;
import com.will.watcher.CommonUtil;
import com.will.watcher.handlebar.BackConfigManager;
import com.will.watcher.handlebar.BackData;
import com.will.watcher.handlebar.ServiceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 2014/6/22.
 * dubbo拦截器
 */
@Component
public class DubboHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DubboHandler.class);

    @Autowired
    private DubboHelper dubboHelper;

    @Autowired
    private BackConfigManager backConfigManager;

    public boolean handle(String path,HttpServletResponse response,Map<String, Object> context){
        if (!"dubbotest".equals(path)){
            return false;
        }
        try{
            //模版
            String serviceTemplate="服务名称：<input type='text' style='width:800px' name='service' value='%s'/><br />";
            String queryTemplate="查询条件：<input type='text' style='width:800px' name='query' value='%s'/><br />";
            String appTemplate="应用名称：<input type='text' style='width:800px' name='app' value='%s'/><br />";
            String yamlTemplate="文件名称：<input type='text' style='width:800px' name='yaml' value='%s'/><br />";
            String jsonTemplate="查询结果：<textarea name='json' style='width:800px;height:400px'>%s</textarea><br />";
            String methodTemplate="<div>%s</div>";
            String fromTemplate="<html><head><meta charset='utf-8'></head><form method='post' action='/dubbotest'>%s<input type='submit' value='提交'/></form></html>";
            //获得变量
            String service="";
            if(context.containsKey("service")&&context.get("service").toString().length()!=0){
                service=context.get("service").toString();
            }
            String serviceHtml=String.format(serviceTemplate,service);
            String query="";
            if(context.containsKey("query")&&context.get("query").toString().length()!=0){
                query=context.get("query").toString();
            }
            String queryHtml=String.format(queryTemplate,query);

            String app="";
            if(context.containsKey("app")&&context.get("app").toString().length()!=0){
                app=context.get("app").toString();
            }
            String appHtml=String.format(appTemplate,app);
            String json="";
            if(context.containsKey("json")&&context.get("json").toString().length()!=0){
                json=context.get("json").toString();
            }
            String yaml="";
            if(context.containsKey("yaml")&&context.get("yaml").toString().length()!=0){
                yaml=context.get("yaml").toString();
            }
            String method="";
            if (service.length()!=0&&app.length()!=0) {
                //调用方法
                if (yaml.length()==0){
                    //根据yaml判断是调用方法还是写入文件
                    json = dubboHelper.invoke(service, app, query);
                    if (json.length() != 0) {
                        yaml = app;
                    }
                }else{
                    BackData backData=new BackData();
                    ServiceData serviceData=new ServiceData();
                    serviceData.setService(service);
                    serviceData.setQuery(query);
                    serviceData.setJson(json);
                    backData.getDatas().put(CommonUtil.getGuid(),serviceData);
                    backConfigManager.setBackData(backData,yaml,true);
                    json="saved success!";
                    yaml="";
                }
            }
            //生成html
            String methodHtml=String.format(methodTemplate,method);
            String yamlHtml=String.format(yamlTemplate,yaml);
            String jsonHtml=String.format(jsonTemplate,json);
            String formHtml=String.format(fromTemplate,serviceHtml+appHtml+methodHtml+queryHtml+jsonHtml+yamlHtml);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(formHtml);
            return true;
        }catch (Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
            return true;
        }
    }
}
