package com.will.watcher.yaml;

import com.google.common.io.Files;
import com.will.watcher.util.Setting;
import com.will.watcher.yaml.model.BackData;
import com.will.watcher.yaml.model.ServiceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tong on 2014/6/21.
 * 后台数据的控制器
 */
@Component
public class BackConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(BackConfigManager.class);

    private Map<String, BackData> dataMap = new HashMap<String, BackData>();

    private List<ServiceData> datalist = new ArrayList<ServiceData>();

    private Yaml YAML = new Yaml();

    @Autowired
    private Setting setting;

    @PostConstruct
    public void init() {
        try {
            String pathName = setting.getDataHome();
            readDataFile(pathName);
            refreshFile(pathName);
        } catch (Exception e) {
            LOG.error("can not run BackConfigManager,path name is:{}",setting.getDataHome());
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
                    path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_CREATE);
                    List<WatchEvent<?>> watchEvents = watchService.take().pollEvents();
                    Thread.sleep(1000);
                    readDataFile(pathName);
                    LOG.info("file reload success");
                } catch (Exception e) {
                    LOG.error("file listener error,{}",e.toString());
                }
            }
            }
        });
        thread.start();
    }

    private void readDataFile(String pathName) throws Exception {
        File directory = new File(pathName);
        if (!directory.exists()){
            directory.mkdir();
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            dataMap.clear();
            for (File file : files) {
                if (file.getName().equals("listener.yaml")) continue;
                if (!file.getName().endsWith(".yaml")) continue;
                FileInputStream fileInputStream=new FileInputStream(file);
                BackData backData = (BackData) YAML.loadAs(fileInputStream, BackData.class);
                fileInputStream.close();
                dataMap.put(file.getName(), backData);
            }
        }
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
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename, isAppend), "UTF-8");
            String firstLine=Files.readFirstLine(new File(filename), Charset.forName("UTF-8"));
            if (firstLine==null||firstLine.length()==0){
                outputStreamWriter.write("datas:\n");
            }
            for (String key : backData.getDatas().keySet()) {
                ServiceData serviceData = backData.getDatas().get(key);
                outputStreamWriter.write("  #"+serviceData.getDesc()+"\n");
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
            LOG.error("can not write yaml file,service:{},file path:{}",backData);
        }
    }
}
