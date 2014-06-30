package com.will.watcher.yaml.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 2014/6/17.
 */
public class BackData implements Serializable {
    private Map<String,ServiceData> datas=new HashMap<String, ServiceData>();

    public Map<String, ServiceData> getDatas() {
        return datas;
    }

    public void setDatas(Map<String, ServiceData> datas) {
        this.datas = datas;
    }
}
