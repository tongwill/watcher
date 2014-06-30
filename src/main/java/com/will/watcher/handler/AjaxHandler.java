package com.will.watcher.handler;

import com.google.common.net.MediaType;
import com.will.watcher.yaml.DataEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 2014/6/21.
 * AJAX拦截器
 */
@Component
public class AjaxHandler {
    @Autowired
    private DataEngine dataEngine;

    public boolean handle(String path, HttpServletResponse response,Map<String, Object> context){
        try{
            String json=dataEngine.getJsonFromData(path, context,false);
            if (json.length()==0){
                return false;
            }
            response.setCharacterEncoding("UTF-8");
            //使用json返回，直接生成对象
            response.setContentType(MediaType.JSON_UTF_8.toString());
            response.getWriter().write(json);
            return true;
        }catch(Exception e){
            return true;
        }
    }
}
