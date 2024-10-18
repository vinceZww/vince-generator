package com.vince.generator;

import com.vince.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心生成器
 */
public class MainGenerator {

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("vince");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("求和结果");
        doGenerate(mainTemplateConfig);
    }

    /**
     * 生成
     * @param model 数据模型
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerate(Object model) throws TemplateException, IOException {
        //1.静态文件生成
        //整个项目的根目录
        String projectPath = System.getProperty("user.dir");

        //输入路径
        String inputPath = projectPath + File.separator + "vince-generator-demo-projects" + File.separator + "acm-template";
        //输出路径
        String outputPath = projectPath;

        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);


        //2.动态文件生成
//        String projectPath = System.getProperty("user.dir") + File.separator + "vince-generator-basic";

        String dynamicInputPath = projectPath + File.separator + "vince-generator-basic" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";

        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
