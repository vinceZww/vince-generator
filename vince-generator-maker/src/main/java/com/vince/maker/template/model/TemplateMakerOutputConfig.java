package com.vince.maker.template.model;

import com.vince.maker.meta.Meta;
import lombok.Data;


/**
 *
 */
@Data
public class TemplateMakerOutputConfig {

    //从未分组的文件中移除组内的同名文件
    private boolean removeGroupFilesFromRoot = true;
}
