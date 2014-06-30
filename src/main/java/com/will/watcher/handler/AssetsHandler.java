package com.will.watcher.handler;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.will.watcher.util.MimeTypes;
import com.will.watcher.util.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@Component
public class AssetsHandler {

    @Autowired
    private Setting setting;

    public boolean handle(String path, HttpServletResponse response) {
        try {
            String lastPath = (String) Iterables.getLast(Splitter.on("/").trimResults().split(path));
            List fileInfo = Splitter.on(".").splitToList(lastPath);
            if (fileInfo.size() == 1) {
                return false;
            }
            response.setContentType(MimeTypes.getType((String) Iterables.getLast(fileInfo)));
            String realPath = setting.getAssetsHome() + "/" +path;
            File file = new File(realPath);
            if (!file.exists()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return true;
            }
            byte[] context = Files.toByteArray(file);
            response.setContentLength(context.length);
            response.getOutputStream().write(context);
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
