package com.vince.maker.model;

import lombok.Data;

/**
 * 动态模板配置
 */
@Data
public class DataModel {

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
