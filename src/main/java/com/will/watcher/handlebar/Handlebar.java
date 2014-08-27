package com.will.watcher.handlebar;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.google.common.collect.Maps;
import com.will.watcher.util.JsonUtil;
import com.will.watcher.yaml.DataEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Handlebar {
    private static final Logger LOG = LoggerFactory.getLogger(Handlebar.class);

    private Handlebars handlebars;

    private DataEngine dataEngine;

    private JsonUtil jsonUtil;

    private GreatTemplateLoader greatTemplateLoader;

    @Autowired
    public Handlebar(DataEngine dataEngine,JsonUtil jsonUtil,GreatTemplateLoader greatTemplateLoader) {
        this.handlebars = new Handlebars(greatTemplateLoader);
        this.dataEngine=dataEngine;
        this.jsonUtil=jsonUtil;
    }

    public <T> void registerHelper(String name, Helper<T> helper) {
        this.handlebars.registerHelper(name, helper);
    }

    public String execPath(String path, Map<String, Object> params, boolean isComponent)
            throws FileNotFoundException {
        try {
            if (params == null)
                params = Maps.newHashMap();
            Template template;
            if (isComponent) {
                template = this.handlebars.compile("component:" + path);
                params.put("_COMP_PATH_", path);
            } else {
                template = this.handlebars.compile(path);
            }
            if (template == null) {
                LOG.error("failed to exec handlebars' template:path={}", path);
                return "";
            }
            return template.apply(params);
        } catch (Exception e) {
            LOG.error("failed to exec handlebars' template:path={}", path);
        }
        return "";
    }

    public String execComponent(String path,Map<String, Object> context) {
        try{
            String json=dataEngine.getJsonFromData(path,context,true);
            if (json.length()!=0){
                LinkedHashMap data=jsonUtil.fromJson(json, LinkedHashMap.class);
                if (data==null){
                    context.put("_DATA_",json);
                }else{
                    context.put("_DATA_",data);
                }
            }
            return execPath(path, context, true);
        }catch(Exception e){
            LOG.error("handlebars can not write json to _DATA_,template path is {}",path);
        }
        return "";
    }
}