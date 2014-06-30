package com.will.watcher.handlebar;

import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.will.watcher.util.Setting;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

@Component
public class GreatTemplateLoader extends AbstractTemplateLoader {
    private ServletContext servletContext;

    private Setting setting;

    @Autowired
    public void init(Setting setting,ServletContext servletContext) {
        this.servletContext = servletContext;
        this.setting=setting;
        setPrefix("/views");
        setSuffix(".hbs");
    }

    public TemplateSource sourceAt(String location) throws IOException {
        Validate.notEmpty(location, "The uri is required.", new Object[0]);
        URL resource = getResource(location);
        if (resource == null) {
            throw new FileNotFoundException(location);
        }
        return new URLTemplateSource(resource.toString(), resource);
    }

    private URL getResource(String location) throws IOException {
        String home = setting.getAssetsHome();
        if (location.startsWith("component:"))
            location = "/components/" + normalize(location.substring(10)) + "/view";
        else {
            location = "/views/" + normalize(location);
        }
        String uri = home + location + getSuffix();
        File file = new File(uri);
        return file.exists() ? file.toURI().toURL() : null;
    }
}