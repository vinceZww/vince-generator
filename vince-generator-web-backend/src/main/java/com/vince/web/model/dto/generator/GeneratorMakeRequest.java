package com.vince.web.model.dto.generator;

import com.vince.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;

/**
 * 制作代码生成器请求
 *
 * @TableName product
 */
@Data
public class GeneratorMakeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 元信息
     */
    private Meta meta;

    /**
     * 模板文件压缩包路径
     */
    private String zipFilePath;

}