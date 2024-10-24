package com.vince.maker.generator.main;

import freemarker.template.TemplateException;

import java.io.IOException;

public class MainGenerator extends GenerateTemplate{

    @Override
    protected void buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        System.out.println("不要给我输出dist");
    }

}
