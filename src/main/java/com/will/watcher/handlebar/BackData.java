package com.will.watcher.handlebar;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 2014/6/17.
 */
@Data
public class BackData implements Serializable {
    private Map<String,ServiceData> datas=new HashMap<String, ServiceData>();
}
