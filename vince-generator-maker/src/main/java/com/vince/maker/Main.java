package com.vince.maker;

//import com.vince.maker.cli.CommandExecutor;

import com.vince.maker.generator.main.GenerateTemplate;
import com.vince.maker.generator.main.MainGenerator;
import com.vince.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
//        GenerateTemplate generateTemplate = new MainGenerator();
        GenerateTemplate generateTemplate = new ZipGenerator();
        generateTemplate.doGenerate();
    }
}
