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

/**
 * Created by pc on 14-5-28.
 */
@Component
public class ExHandlebarHelpers {
    @Autowired
    private Handlebar handlebar;

    @PostConstruct
    public void init() {

        this.handlebar.registerHelper("formatTime", new Helper<Long>() {
            public CharSequence apply(Long date, Options options)
                    throws IOException
            {
                if (date == null) {
                    return "";
                }
                String format = (String)options.param(0, "default");
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(date);
                return new SimpleDateFormat(options.param(0,"yyyy-MM-dd").toString()).format(calendar.getTime());
            }
        });

        this.handlebar.registerHelper("notEquals", new Helper(){
            public CharSequence apply(Object source, Options options) throws IOException
            {
                if (!Objects.equal(String.valueOf(source), String.valueOf(options.param(0)))) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
    }
}
