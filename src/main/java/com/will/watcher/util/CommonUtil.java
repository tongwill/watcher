package com.will.watcher.util;

import java.util.Map;
import java.util.UUID;

public class CommonUtil {
	public static String getGuid(){
		return UUID.randomUUID().toString().replace("-", "");
	}

    public static String getQueryFromContext(Map<String, Object> context){
        String query="";
        String[] filters=new String[]{"random","cookie","_USER_","_COMP_PATH_","_COMP_NAME_","_COMP_DATA_","_DESIGN_MODE_","_PATH_","_USER_","_PAGE_","_CDATA_KEYS_","_DATA_","_ERROR_","_STYLE_","_CLASS_","_HREF_"};
        for (String param:context.keySet()){
            boolean isHas=false;
            for (String filter : filters){
                if (isHas=filter.equals(param)){
                    break;
                }
            }
            if (!isHas){
                query=query+param+"="+context.get(param).toString()+"&";
            }
        }
        if (query.length()!=0){
            query=query.substring(0,query.length()-1);
        }
        return query;
    }
}
