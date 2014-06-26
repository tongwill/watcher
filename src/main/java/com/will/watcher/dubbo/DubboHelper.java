package com.will.watcher.dubbo;

import io.terminus.common.utils.JsonMapper;
import io.terminus.pampas.engine.config.model.Service;
import io.terminus.pampas.engine.mapping.DubboExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tong on 2014/6/23.
 */
@Component
public class DubboHelper {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DubboExecutor dubboExecutor;

    public String invoke(String uri,String app,String query)throws Exception{
        Service service=new Service();
        service.setUri(uri);
        service.setApp(app);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if (query.length()!=0){
            String[] params=query.split("&");
            for (String param:params){
                paramMap.put(param.split("=")[0],param.split("=")[1]);
            }
        }
        return this.invoke(service,paramMap);
    }

    public String invoke(Service service,Map<String, Object> paramMap)throws Exception{
        String json="";
        //调用dubbo
        Object result=dubboExecutor.exec(service,paramMap);
        //判断返回值的类别
        if (result instanceof  String){
            json=((String) result).toString();
        }else if(result instanceof  LinkedHashMap){
            json=JsonMapper.nonEmptyMapper().toJson(result);
        }
        return json;
    }
}
