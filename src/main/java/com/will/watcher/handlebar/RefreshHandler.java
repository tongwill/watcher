package com.will.watcher.handlebar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 2014/6/21.
 * 刷新data文件内容
 */
@Component
public class RefreshHandler {
    @Autowired
    private BackConfigManager backConfigManager;

    public boolean handle(String path, HttpServletResponse response,Map<String, Object> context){
        try{
            if (!"refresh".equals(path)){
                return false;
            }
            backConfigManager.init();
            return true;
        }catch(Exception e){
            return true;
        }
    }
}
