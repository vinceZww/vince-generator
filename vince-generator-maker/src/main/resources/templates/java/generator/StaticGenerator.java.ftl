package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成器
 */
public class StaticGenerator {

    /**
     * 拷贝文件（Hutool实现）
     * @param inputFile 输入路径
     * @param outputFile 输出路径
     */
    public static void copyFilesByHutool(String inputFile, String outputFile) {
        FileUtil.copy(inputFile, outputFile, false);
    }


}
