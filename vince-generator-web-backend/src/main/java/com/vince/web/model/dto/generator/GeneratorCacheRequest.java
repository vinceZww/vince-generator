package com.vince.web.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 缓存代码生成器请求
 *
 */
@Data
public class GeneratorCacheRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成器的id
     */
    private Long id;

}