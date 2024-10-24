package com.vince.maker;

//import com.vince.maker.cli.CommandExecutor;

import com.vince.maker.generator.main.MainGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        MainGenerator generator = new MainGenerator();
        generator.doGenerate();
    }
}
