package com.will.watcher.dubbo;

import com.will.watcher.util.CommonUtil;
import com.will.watcher.util.WatcherVariable;
import com.will.watcher.yaml.model.ServiceData;
import io.terminus.pampas.engine.config.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by tong on 2014/6/24.
 * 请求监听器
 */
@Component
public class DubboListener {
    @Autowired
    private DubboHelper dubboHelper;

    private static final Logger LOG = LoggerFactory.getLogger(DubboListener.class);

    public String listener(Service service,Map<String, Object> context){
        try{
            String json="";
            try{
                json=dubboHelper.invoke(service,context);
            }catch(Exception e){
                LOG.error("listener error,can not invoke the dubbo service:{}",service.getUri());
            }
            ServiceData data=new ServiceData();
            data.setService(service.getUri());
            data.setQuery(CommonUtil.getQueryFromContext(context));
            data.setJson(json);
            data.setDesc(service.getDesc());
            String key=CommonUtil.getGuid();
            WatcherVariable.BACK_DATA.getDatas().put(key,data);
            return json;
        }catch(Exception e){
            LOG.error("listener error,can not save! service:{}",service.getUri());
        }
        return  "";
    }
}
