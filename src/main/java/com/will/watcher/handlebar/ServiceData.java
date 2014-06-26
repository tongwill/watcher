package com.will.watcher.handlebar;

import java.io.Serializable;

/**
 * Created by tong on 2014/6/17.
 */
public class ServiceData implements Serializable {
    private String service;
    private String query;
    private String json;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
