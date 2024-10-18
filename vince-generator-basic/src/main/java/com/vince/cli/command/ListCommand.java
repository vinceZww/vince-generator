package com.vince.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@CommandLine.Command(name = "list",description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable {


    public void run() {
        String projectPath = System.getProperty("user.dir");

        //这里要注意，看打开的位置，需不需要获取父类文件路径，在idea中运行用第一种
        File parentFile = new File(projectPath);
//        File parentFile = new File(projectPath).getParentFile();
//        System.out.println("parentFile:" + parentFile);
        //输入路径
        String inputPath =new File(parentFile,"vince-generator-demo-projects/acm-template").getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
