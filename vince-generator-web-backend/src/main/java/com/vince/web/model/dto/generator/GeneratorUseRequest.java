package com.vince.web.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 使用代码生成器请求
 *
 * @TableName product
 */
@Data
public class GeneratorUseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成器的id
     */
    private Long id;

    /**
     * 数据模型
     */
    private Map<String,Object> dataModel;

}