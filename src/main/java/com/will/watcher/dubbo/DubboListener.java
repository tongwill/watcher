package com.will.watcher.dubbo;

import com.google.common.base.Throwables;
import com.will.watcher.CommonUtil;
import com.will.watcher.WatcherVariable;
import com.will.watcher.handlebar.ServiceData;
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
            String json=dubboHelper.invoke(service,context);
            ServiceData data=new ServiceData();
            data.setService(service.getUri());
            data.setQuery(CommonUtil.getQueryFromContext(context));
            data.setJson(json);
            String key=CommonUtil.getGuid();
            WatcherVariable.BACK_DATA.getDatas().put(key,data);
            return json;
        }catch(Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return  "";
    }
}
