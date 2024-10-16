package com.vince.generator;

import com.vince.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {

        //1.静态文件生成
        //相对路径
        String projectPath = System.getProperty("user.dir");
        //输入路径
        String inputPath = projectPath+ File.separator+ "vince-generator-demo-projects" + File.separator + "acm-template";
        //输出路径
        String outputPath = projectPath;

        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);


        //2.动态文件生成
//        String projectPath = System.getProperty("user.dir") + File.separator + "vince-generator-basic";

        String dynamicInputPath = projectPath + File.separator + "vince-generator-basic" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";

        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("vince");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("求和结果：");
        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, mainTemplateConfig);

    }
}
