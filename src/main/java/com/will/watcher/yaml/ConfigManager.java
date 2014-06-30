package com.will.watcher.yaml;

import com.google.common.base.Throwables;
import com.will.watcher.util.Setting;
import io.terminus.pampas.engine.config.model.BackConfig;
import io.terminus.pampas.engine.config.model.FrontConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
public class ConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
    private FrontConfig frontConfig = new FrontConfig();
    private BackConfig backConfig = new BackConfig();

    @Autowired
    private Setting setting;

    @PostConstruct
    private void init() {
        try{
            Yaml YAML = new Yaml();
            frontConfig=(FrontConfig)YAML.loadAs(new FileInputStream(new File(setting.getAssetsHome()+"\\front_config.yaml")), FrontConfig.class);
            backConfig=(BackConfig)YAML.loadAs(new FileInputStream(new File(setting.getAssetsHome()+"\\back_config.yaml")), BackConfig.class);
        }catch(FileNotFoundException e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    public io.terminus.pampas.engine.config.model.Component findComponent(String componentPath) {
        if ((frontConfig == null) || (frontConfig.getComponents() == null)) {
            return null;
        }
        return (io.terminus.pampas.engine.config.model.Component)frontConfig.getComponents().get(componentPath);
    }

    public FrontConfig getFrontConfig() {
        return frontConfig;
    }

    public BackConfig getBackConfig() {
        return backConfig;
    }
}

