package com.will.watcher.handlebar;

import com.google.common.base.Throwables;
import io.terminus.pampas.engine.model.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tong on 2014/6/21.
 * 后台数据的控制器
 */
@Component
public class BackConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(BackConfigManager.class);

    private static final Yaml YAML = new Yaml();

    private BackData backData=new BackData();

    @Autowired
    private App app;

    @PostConstruct
    public void init(){
        try{
            List<BackData> backDataList=new ArrayList<BackData>();
            File directory=new File(app.getAssetsHome()+"/data");
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for(File file:files){
                    if (file.getName().equals("listener.yaml")) continue;
                    BackData temp=(BackData)YAML.loadAs(new FileInputStream(file), BackData.class);
                    backDataList.add(temp);
                }
                if (backDataList.size()>1){
                    for (BackData temp:backDataList){
                        backData.getDatas().putAll(temp.getDatas());
                    }
                }else if(backDataList.size()==1){
                    backData=backDataList.get(0);
                }
            }
        }catch (Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }
    /**
     * 从后台配置文件中获取数据返回前台
     * @return
     */
    public BackData getBackData() {
       return this.backData;
    }

    /**
     * 写入后台数据文件
     * @param backData 对象
     * @param fileName 文件名称（默认在前端项目中的files/data文件夹下建立文件）
     * @param isAppend 是否在文件末尾添加
     */
    public void setBackData(BackData backData,String fileName,boolean isAppend) {
        try{
            String filename=app.getAssetsHome().substring(0, app.getAssetsHome().length() - 7)+"app/files/data/"+fileName+".yaml";
            String firstline="";
            try{
                BufferedReader bufferedReader=new BufferedReader(new FileReader(filename));
                firstline=bufferedReader.readLine();
            }catch(FileNotFoundException e){

            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename,isAppend),"UTF-8");
            if (!"datas:".equals(firstline)){
                outputStreamWriter.write("datas:\n");
            }
            for (String key : backData.getDatas().keySet()){
                ServiceData serviceData=backData.getDatas().get(key);
                outputStreamWriter.write("  \""+key+"\":\n");
                outputStreamWriter.write("    service: \""+serviceData.getService()+"\"\n");
                if (serviceData.getQuery()!=null&&serviceData.getQuery().length()!=0){
                    outputStreamWriter.write("    query: \""+serviceData.getQuery()+"\"\n");
                }
                outputStreamWriter.write("    json: '"+serviceData.getJson()+"'\n");
            }
            outputStreamWriter.flush();
            outputStreamWriter.close();
        }catch (Exception e){
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }
}
