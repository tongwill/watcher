package com.will.watcher.yaml;

import com.google.common.base.Throwables;
import com.will.watcher.util.Setting;
import com.will.watcher.yaml.model.BackData;
import com.will.watcher.yaml.model.ServiceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Created by tong on 2014/6/21.
 * 后台数据的控制器
 */
@Component
public class BackConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(BackConfigManager.class);

    private Map<String, BackData> dataMap = new HashMap<String, BackData>();

    private List<ServiceData> datalist = new ArrayList<ServiceData>();

    @Autowired
    private Setting setting;

    @PostConstruct
    public void init() {
        try {
            String pathName = setting.getDataHome();
            refreshFile(pathName);
            //初始化文件
            File directory = new File(pathName);
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    readDataFile(file, StandardWatchEventKinds.ENTRY_CREATE);
                }
            }
            convertBackData();
        } catch (Exception e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    private void refreshFile(final String pathName) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        WatchService watchService = FileSystems.getDefault().newWatchService();
                        Path path = Paths.get(pathName);
                        // 注册监听器
                        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                        List<WatchEvent<?>> watchEvents = watchService.take().pollEvents();
                        for (WatchEvent<?> watchEvent : watchEvents) {
                            readDataFile(new File(pathName+"/"+ watchEvent.context().toString()), watchEvent.kind());
                            LOG.info("file reload success");
                        }
                        convertBackData();
                    } catch (Exception e) {
                        LOG.error(Throwables.getStackTraceAsString(e));
                    }
                }
            }
        });
        thread.start();
    }

    private void readDataFile(File file, WatchEvent.Kind fileKind) throws Exception {
        if (file.getName().equals("listener.yaml")) return;
        if (fileKind == StandardWatchEventKinds.ENTRY_DELETE) {
            dataMap.remove(file.getName());
            return;
        }
        Yaml YAML = new Yaml();
        BackData backData = (BackData) YAML.loadAs(new FileInputStream(file), BackData.class);
        dataMap.put(file.getName(), backData);
    }

    private void convertBackData() {
        datalist.clear();
        for (BackData backData : dataMap.values()) {
            for (ServiceData serviceData : backData.getDatas().values()) {
                datalist.add(serviceData);
            }
        }
    }

    /**
     * 从后台配置文件中获取数据返回前台
     *
     * @return
     */
    public List<ServiceData> getServiceDataList() {
        return this.datalist;
    }

    /**
     * 写入后台数据文件
     *
     * @param backData 对象
     * @param fileName 文件名称（默认在前端项目中的files/data文件夹下建立文件）
     * @param isAppend 是否在文件末尾添加
     */
    public void setBackData(BackData backData, String fileName, boolean isAppend) {
        try {
            String filename =setting.getDataHome()+"/"+ fileName + ".yaml";
            String firstline = "";
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
                firstline = bufferedReader.readLine();
            } catch (FileNotFoundException e) {

            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename, isAppend), "UTF-8");
            if (!"datas:".equals(firstline)) {
                outputStreamWriter.write("datas:\n");
            }
            for (String key : backData.getDatas().keySet()) {
                ServiceData serviceData = backData.getDatas().get(key);
                outputStreamWriter.write("  \"" + key + "\":\n");
                outputStreamWriter.write("    service: \"" + serviceData.getService() + "\"\n");
                if (serviceData.getQuery() != null && serviceData.getQuery().length() != 0) {
                    outputStreamWriter.write("    query: \"" + serviceData.getQuery() + "\"\n");
                }
                outputStreamWriter.write("    json: '" + serviceData.getJson() + "'\n");
            }
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (Exception e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }
}
