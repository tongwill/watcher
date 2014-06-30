package com.will.watcher.handler;

import com.google.common.base.Throwables;
import com.will.watcher.util.WatcherVariable;
import com.will.watcher.dubbo.DubboHelper;
import com.will.watcher.yaml.BackConfigManager;
import com.will.watcher.yaml.model.BackData;
import com.will.watcher.yaml.DataEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 2014/6/23.
 * listenerstart与listenerstop的拦截器
 */
@Component
public class ListenerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerHandler.class);

    @Autowired
    private DataEngine dataEngine;

    @Autowired
    private DubboHelper dubboHelper;

    @Autowired
    private BackConfigManager backConfigManager;

    private static final Yaml YAML = new Yaml();

    public boolean handle(String path, HttpServletResponse response, Map<String, Object> context) {
        try{
            if ("startlistener".equals(path)){
                WatcherVariable.IS_DUBBO_LISTENER=true;
                WatcherVariable.BACK_DATA=new BackData();
                response.getWriter().write("listener started!");
                return true;
            }else if ("stoplistener".equals(path)){
                if (!WatcherVariable.IS_DUBBO_LISTENER){
                    response.getWriter().write("listener didn't started!");
                    return true;
                }
                //写入文件
                WatcherVariable.IS_DUBBO_LISTENER=false;
                backConfigManager.setBackData(WatcherVariable.BACK_DATA,"listener",false);
                response.getWriter().write("listener stopped!");
                WatcherVariable.BACK_DATA=new BackData();
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
            return true;
        }
    }
}
