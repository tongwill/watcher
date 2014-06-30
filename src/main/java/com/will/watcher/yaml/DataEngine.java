package com.will.watcher.yaml;

import com.will.watcher.util.WatcherVariable;
import com.will.watcher.dubbo.DubboListener;
import com.will.watcher.yaml.model.ServiceData;
import io.terminus.pampas.engine.config.model.Mapping;
import io.terminus.pampas.engine.config.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by tong on 2014/6/21.
 */
@Component
public class DataEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DataEngine.class);

    @Autowired
    private BackConfigManager backConfigManager;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private DubboListener dubboListener;


    private String getComponentFront(String path){
        String front="";
        for(String key : configManager.getFrontConfig().getComponents().keySet()){
            if(key.equals(path)){
                front= configManager.getFrontConfig().getComponents().get(key).getService();
                break;
            }
        }
        return front;
    }

    private String getMappingFront(String path){
        String front="";
        for(Mapping mapping : configManager.getFrontConfig().getMappings()){
            if(mapping.getPattern().equals(path)){
                front=mapping.getService();
                break;
            }
        }
        return front;
    }

    private Service getBack(String front){
        if(front.indexOf(":")!=-1){
            front=front.split(":")[1];
        }
        for(String key : configManager.getBackConfig().getServices().keySet()){
            if(key.equals(front)){
                return configManager.getBackConfig().getServices().get(key);
            }
        }
        return null;
    }

    public Service getBackService(String path,boolean isComponent){
        String front="";
        if (isComponent){
            front=getComponentFront(path);
        }else{
            front=getMappingFront(path);
        }
        if (front.length()==0){
            return null;
        }
        return getBack(front);
    }

    public Service getBackService(String path){
        String front="";
        front=getComponentFront(path);
        if (front.length()==0) {
            front = getMappingFront(path);
        }
        if (front.length()==0){
            return null;
        }
        return getBack(front);
    }

    public String getJsonFromData(String path,Map<String, Object> context,boolean isComponent){
        Service service=getBackService(path,isComponent);
        if (service==null){
            return "";
        }
        if (WatcherVariable.IS_DUBBO_LISTENER){
            return dubboListener.listener(service,context);
        }
        String back=service.getUri();
        for (ServiceData serviceData:backConfigManager.getServiceDataList()){
            if(serviceData.getService().equals(back)){
                if (serviceData.getQuery()!=null&&serviceData.getQuery().length()!=0){
                    String[] params=serviceData.getQuery().split("&");
                    int queryEquals=0;
                    for (String param:params){
                        if ("${any}".equals(param.split("=")[1])){
                            queryEquals++;
                            continue;
                        }
                        for (String query:context.keySet()){
                            if (!"_USER_".equals(query)){
                                if (query.equals(param.split("=")[0])&&context.get(query).toString().equals(param.split("=")[1])){
                                    queryEquals++;
                                    break;
                                }
                            }
                        }
                    }
                    if (queryEquals==params.length){
                        return serviceData.getJson();
                    }else{
                        continue;
                    }
                }
                else{
                    return serviceData.getJson();
                }
            }
        }
        LOG.warn("no data mapping");
        return "";
    }
}
