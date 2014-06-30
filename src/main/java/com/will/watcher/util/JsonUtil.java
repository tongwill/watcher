package com.will.watcher.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by tong on 2014/6/27.
 */
@Component
public class JsonUtil {

    private static Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        this.mapper = new ObjectMapper();
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.registerModule(new GuavaModule());
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return this.mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    public String toJson(Object object) {
        try {
            return this.mapper.writeValueAsString(object);
        } catch (IOException e) {
            LOG.warn("write to json string error:" + object, e);
        }
        return null;
    }
}
