package com.will.watcher.dubbo;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.google.common.base.Defaults;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.terminus.pampas.client.Agent;
import io.terminus.pampas.client.ParamUtil;
import io.terminus.pampas.client.WrapResp;
import io.terminus.pampas.common.BaseUser;
import io.terminus.pampas.common.InnerCookie;
import io.terminus.pampas.common.UserNotLoginException;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.config.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DubboExecutor {
    @Autowired
    private DubboHelper dubboHelper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BaseUser baseUser;

    private DefaultConversionService converter = new DefaultConversionService();

    private LoadingCache<Service, LinkedHashMap<String, Class>> methodInfoCache;

    private LoadingCache<String, Optional<Agent>> referenceCache;

    private static final Logger LOG = LoggerFactory.getLogger(DubboListener.class);

    @PostConstruct
    private void init() {
        this.methodInfoCache = CacheBuilder.newBuilder().build(new CacheLoader<Service, LinkedHashMap<String, Class>>() {
            public LinkedHashMap<String, Class> load(Service service) throws Exception {
                Agent agent = DubboExecutor.this.getAgent(service.getApp());
                LinkedHashMap<String, String> paramsInfo = agent.getParamsInfo(service.getUri());
                LinkedHashMap params = Maps.newLinkedHashMap();
                for (String name : paramsInfo.keySet()) {
                    String className = (String) paramsInfo.get(name);
                    try {
                        Class paramClass = Class.forName(className);
                        params.put(name, paramClass);
                    } catch (ClassNotFoundException e) {
                        params.put(name, DubboExecutor.UnKnowClass.class);
                        LOG.error("error when init dubbo reference bean. class {}, version {}", new Object[] { Agent.class, service.getApp()});
                    }
                }
                return params;
            }
        });
        this.referenceCache = CacheBuilder.newBuilder().build(new CacheLoader<String,Optional<Agent>>()
        {
            public Optional<Agent> load(String app) throws Exception {
                ReferenceBean referenceBean = new ReferenceBean();
                referenceBean.setApplicationContext(applicationContext);
                referenceBean.setInterface(Agent.class);
                if ((!Strings.isNullOrEmpty(app)) && (!Objects.equal(app, "DEFAULT"))) {
                    referenceBean.setVersion(app);
                }
                try{
                    referenceBean.afterPropertiesSet();
                    return Optional.of((Agent)referenceBean.get());
                } catch (Exception e) {
                    LOG.error("error when init dubbo reference bean. class {}, version {}", new Object[] { Agent.class, app});
                }
                return Optional.absent();
            }
        });
    }

    public Object exec(Service service, Map<String, Object> params) {
        LinkedHashMap<String, Class> methodParams = (LinkedHashMap) this.methodInfoCache.getUnchecked(service);
        Map<String,Object> args = Maps.newHashMap();
        boolean needContext = false;
        for (String paramName : methodParams.keySet()) {
            Class paramClass = (Class) methodParams.get(paramName);
            Object arg = convertParam(paramName, paramClass, params);
            if (arg != null) {
                args.put(paramName, arg);
            } else if (!ParamUtil.isPrimitive(paramClass)) {
                needContext = true;
            }
        }
        Agent agent = getAgent(service.getApp());
        WrapResp wrapResp = agent.call(service.getUri(), args, needContext ? params : null,baseUser);
        if (wrapResp.getCookie() != null) {
            UserUtil.getInnerCookie().merge(wrapResp.getCookie());
        }
        return wrapResp.getResult();
    }

    private Object convertParam(String paramName, Class<?> paramClass, Map<String, Object> params){
        if (BaseUser.class.isAssignableFrom(paramClass)) {
            Object user = UserUtil.getCurrentUser();
            if (user == null) {
                throw new UserNotLoginException("user not login.");
            }
            return user;
        }
        if (paramClass == InnerCookie.class) {
            return UserUtil.getInnerCookie();
        }
        if (Map.class.isAssignableFrom(paramClass))
        {
            return filterCustomObject(params);
        }
        Object param = params.get(paramName);

        if (!ParamUtil.isPrimitive(paramClass)) {
            return param;
        }
        if (param == null) {
            return Defaults.defaultValue(paramClass);
        }

        return converter.convert(param, paramClass);
    }

    private Map<String, String> filterCustomObject(Map<String, Object> params) {
        Map targetParam = Maps.newHashMapWithExpectedSize(params.size());
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value != null) {
                if (ParamUtil.isPrimitive(value))
                    targetParam.put(key, String.valueOf(value));
            }
        }
        return targetParam;
    }

    private Agent getAgent(String app) {
        Optional<Agent> referenceOptional = (Optional<Agent>)this.referenceCache.getUnchecked(app);
        if (referenceOptional.isPresent()){
            return (Agent)referenceOptional.get();
        }else{
            this.referenceCache.invalidate(app);
        }
        LOG.warn("Agent is null,app name is "+app);
        return null;
    }

    public static abstract interface UnKnowClass{}
}
