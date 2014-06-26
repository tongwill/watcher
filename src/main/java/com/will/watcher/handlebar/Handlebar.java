package com.will.watcher.handlebar;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.pampas.engine.handlebars.GreatTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Handlebar {
    private static final Logger LOG = LoggerFactory.getLogger(Handlebar.class);

    private Handlebars handlebars;

    private DataEngine dataEngine;

    @Autowired
    public Handlebar(DataEngine dataEngine,ServletContext servletContext) {
        TemplateLoader templateLoader = new GreatTemplateLoader(servletContext, "/views", ".hbs");
        this.handlebars = new Handlebars(templateLoader);
        this.dataEngine=dataEngine;
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
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return "";
    }

    public String execComponent(String path,Map<String, Object> context) {
        try{
            String json=dataEngine.getJsonFromData(path,context,true);
            context.put("_DATA_",JsonMapper.nonEmptyMapper().fromJson(json, LinkedHashMap.class));
            return execPath(path, context, true);
        }catch(Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return "";
    }
}