package com.will.watcher.handlebar;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.html.HtmlEscapers;
import io.terminus.common.utils.JsonMapper;
import io.terminus.pampas.engine.Setting;
import io.terminus.pampas.engine.config.ConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
public class RenderHelpers {
    private static final Logger log = LoggerFactory.getLogger(RenderHelpers.class);

    @Autowired
    private Handlebar handlebar;

    @Autowired
    private ConfigManager configManager;

    @PostConstruct
    private void init() {
        this.handlebar.registerHelper("inject", new Helper<String>() {
            public CharSequence apply(String compPath, Options options) throws IOException {
                boolean isDesignMode = options.get("_DESIGN_MODE_") != null;

                Map tempContext = Maps.newHashMap();
                if ((options.context.model() instanceof Map)) {
                    tempContext.putAll((Map) options.context.model());

                    Set<String> cdataKeys = (Set) tempContext.remove("_CDATA_KEYS_");
                    if (cdataKeys != null) {
                        for (String key : cdataKeys) {
                            tempContext.remove(key);
                        }
                    }
                    if (isDesignMode) {
                        tempContext.remove("_COMP_DATA_");
                    }
                }
                if ((options.tagType == TagType.SECTION) && (StringUtils.isNotBlank(options.fn.text()))) {
                    Map config = (Map) JsonMapper.nonEmptyMapper().fromJson(options.fn.text(), Map.class);
                    if ((config != null) && (!config.isEmpty())) {
                        tempContext.put("_CDATA_KEYS_", config.keySet());
                        tempContext.putAll(config);
                        if (isDesignMode) {
                            tempContext.put("_COMP_DATA_", HtmlEscapers.htmlEscaper().escape(options.fn.text().trim()));
                        }
                    }
                }
                io.terminus.pampas.engine.config.model.Component component = new io.terminus.pampas.engine.config.model.Component();
                component.setPath(compPath);
                Object firstParam = options.param(0, null);
                if ((firstParam != null) && ((firstParam instanceof String)) && (StringUtils.isNotBlank((String) firstParam))) {
                    component.setService((String) firstParam);
                } else {
                    io.terminus.pampas.engine.config.model.Component result = RenderHelpers.this.configManager.findComponent(Setting.getCurrentAppKey(), compPath);
                    if (result == null)
                        RenderHelpers.log.warn("can't find component config for path:{}", compPath);
                    else {
                        component = result;
                    }
                }
                if (isDesignMode) {
                    String[] paths = compPath.split("/");
                    tempContext.put("_COMP_NAME_", Objects.firstNonNull(component.getName(), paths[(paths.length - 1)]));
                }
                return new Handlebars.SafeString(RenderHelpers.this.handlebar.execComponent(compPath, tempContext));
            }
        });
//        this.handlebar.registerHelper("inject", new Helper<String>() {
//            public CharSequence apply(String compPath, Options options) throws IOException {
//                boolean isDesignMode = options.get("_DESIGN_MODE_") != null;
//                Map tempContext = Maps.newHashMap();
//                if ((options.context.model() instanceof Map)) {
//                    tempContext.putAll((Map) options.context.model());
//                    Set<String> cdataKeys = (Set) tempContext.remove("_CDATA_KEYS_");
//                    if (cdataKeys != null) {
//                        for (String key : cdataKeys) {
//                            tempContext.remove(key);
//                        }
//                    }
//                    if (isDesignMode) {
//                        tempContext.remove("_COMP_DATA_");
//                    }
//                }
//                return new Handlebars.SafeString(handlebar.execComponent(compPath, tempContext));
//            }
//        });
        this.handlebar.registerHelper("component", new Helper<String>() {
            public CharSequence apply(String className, Options options)
                    throws IOException {
                boolean isDesignMode = options.get("_DESIGN_MODE_") != null;
                className = className + " eve-component";
                Object customClassName = options.context.get("_CLASS_");
                StringBuilder compOpenTag = new StringBuilder("<div class=\"").append(className);
                if (customClassName != null) {
                    compOpenTag.append(" ").append(customClassName);
                }
                compOpenTag.append("\"");
                Object style = options.context.get("_STYLE_");
                if (style != null) {
                    compOpenTag.append(" style=\"").append(style).append("\"");
                }
                Object compName = options.context.get("_COMP_NAME_");
                if (compName != null) {
                    compOpenTag.append(" data-comp-name=\"").append(compName).append("\"");
                }
                Object compData = options.context.get("_COMP_DATA_");
                if (compData != null) {
                    compOpenTag.append(" data-comp-data=\"").append(compData).append("\"");
                }
                Object compPath = options.context.get("_COMP_PATH_");
                if (compPath != null) {
                    compOpenTag.append(" data-comp-path=\"").append(compPath).append("\"");
                }
                if (isDesignMode) {
                    compOpenTag.append(" data-comp-class=\"").append(className).append("\"");
                }
                compOpenTag.append(" >");
                return new Handlebars.SafeString(compOpenTag.toString() + options.fn() + "</div>");
            }
        });
    }
}