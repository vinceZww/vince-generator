package com.vince.maker.generator.file;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成器
 */
public class StaticFileGenerator {

    /**
     * 拷贝文件（Hutool实现）
     * @param inputFile 输入路径
     * @param outputFile 输出路径
     */
    public static void copyFileByHutool(String inputFile, String outputFile) {
        FileUtil.copy(inputFile, outputFile, false);
    }


}
