package com.will.watcher.handlebar;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Created by pc on 14-5-28.
 */
@Component
public class CdkHandlebarHelpers {
    @Autowired
    private Handlebar handlebarEngine;

    @PostConstruct
    public void init() {

        this.handlebarEngine.registerHelper("formatTime", new Helper<Long>() {
            public CharSequence apply(Long date, Options options)
                    throws IOException{
                if (date == null) {
                    return "";
                }
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(date);
                return new SimpleDateFormat(options.param(0,"yyyy-MM-dd").toString()).format(calendar.getTime());
            }
        });

        this.handlebarEngine.registerHelper("notEquals", new Helper(){
            public CharSequence apply(Object source, Options options) throws IOException
            {
                if (!Objects.equal(String.valueOf(source), String.valueOf(options.param(0)))) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebarEngine.registerHelper("hoverDetail" , new Helper(){
            public CharSequence apply(Object o, Options options) throws IOException{
                String ret = "<ul>";
                List list=(List)o;
                if(list==null){
                    //list=new ArrayList();
                    return "";
                }
                for(int i=0;i<list.size(); i++) {
                    ret = ret + "<li>" + list.get(i) + "</li>";
                }
                return ret + "</ul>";
            }

        });
        this.handlebarEngine.registerHelper("equalsSize" , new Helper<Object>(){
            public CharSequence apply(Object object, Options options) throws IOException {
                if (object instanceof String) {
                    if (((String) object).length() == ((Integer) options.param(0)).intValue()) {
                        return options.fn();
                    } else {
                        options.inverse();
                    }
                } else if (object instanceof Collection) {
                    if (((Collection) object).size() == ((Integer) options.param(0)).intValue()) {
                        return options.fn();
                    } else {
                        options.inverse();
                    }
                }
                return options.inverse();
            }
        });
        this.handlebarEngine.registerHelper("gtSize" , new Helper<Object>(){
            public CharSequence apply(Object object, Options options) throws IOException {
                if (object instanceof String) {
                    if (((String) object).length() > ((Integer) options.param(0)).intValue()) {
                        return options.fn();
                    } else {
                        options.inverse();
                    }
                } else if (object instanceof Collection) {
                    if (((Collection) object).size() > ((Integer) options.param(0)).intValue()) {
                        return options.fn();
                    } else {
                        options.inverse();
                    }
                }
                return options.inverse();
            }
        });
    }
}
