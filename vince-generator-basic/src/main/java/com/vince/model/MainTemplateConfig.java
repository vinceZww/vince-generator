package com.vince.model;

import lombok.Data;

@Data
public class MainTemplateConfig {


    /**
     * 作者
     */
    private String author = "vince";

    /**
     * 输出信息
     */
    private String outputText = "输出结果";

    /**
     * 是否循环
     */
    private boolean loop;




}
