package com.will.watcher.dubbo;

import com.google.common.base.Splitter;
import com.will.watcher.util.JsonUtil;
import io.terminus.pampas.engine.config.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by tong on 2014/6/23.
 */
@Component
public class DubboHelper {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DubboExecutor dubboExecutor;

    @Autowired
    private JsonUtil jsonUtil;

    public String invoke(String uri,String app,String query,Map<String, Object> context)throws Exception{
        Service service=new Service();
        service.setUri(uri);
        service.setApp(app);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if (query.length()!=0){
            List<String> params= Splitter.on("&").splitToList(query);
            for (String param:params){
                Iterator<String> iterator=Splitter.on("=").split(param).iterator();
                paramMap.put(iterator.next(),iterator.next());
            }
        }
        for (String key:context.keySet()){
            if (key.startsWith("_")&&key.endsWith("_")){
                paramMap.put(key,context.get(key));
            }
        }
        return this.invoke(service,paramMap);
    }

    public String invoke(Service service,Map<String, Object> paramMap)throws Exception{
        String json="";
        //调用dubbo
        Object result=dubboExecutor.exec(service,paramMap);
        if(result!=null){
            //判断返回值的类别
            if (result instanceof  String){
                json=((String) result).toString();
            }else if(result instanceof  LinkedHashMap){
                json=jsonUtil.toJson(result);
            }
        }
        return json;
    }
}
