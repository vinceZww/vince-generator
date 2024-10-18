package com.vince.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.vince.generator.MainGenerator;
import com.vince.model.MainTemplateConfig;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate",description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {

    /**
     * 作者
     */
    @CommandLine.Option(names = {"-a", "--author"}, arity = "0..1", description = "作者名称", interactive = true, echo = true)
    private String author = "vince";

    /**
     * 输出信息
     */
    @CommandLine.Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出文档", interactive = true, echo = true)
    private String outputText = "输出结果";

    /**
     * 是否循环
     */
    @CommandLine.Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否循环", interactive = true, echo = true)
    private boolean loop;


    public Integer call() throws Exception {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        BeanUtil.copyProperties(this, mainTemplateConfig);
        MainGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}
