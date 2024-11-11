package com.vince.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.vince.maker.meta.Meta;
import com.vince.maker.meta.enums.FileGenerateTypeEnum;
import com.vince.maker.meta.enums.FileTypeEnum;
import com.vince.maker.template.enums.FileFilterRangeEnum;
import com.vince.maker.template.enums.FileFilterRuleEnum;
import com.vince.maker.template.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {

    /**
     * 做做模板
     * @param templateMakerConfig
     * @return
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();

        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, outputConfig, id);
    }


    /**
     * 制作模板
     *
     * @param newMeta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {
        //没有id则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        //复制目录
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;

        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        //一、输入信息
        //输入文件信息
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        //注意win系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        //二、制作文件模板
        List<Meta.FileConfig.FileInfo> newFilesInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        //处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        //要挖坑的项目根目录
//        File tempFile = new File(templatePath);
//        templatePath = tempFile.getAbsolutePath();

        //三、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

        //已有meta文件，不是第一次制作，则在meta基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            //1.追加配置
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFilesInfoList);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            //配置去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            //模型去重
            newMeta.getModelConfig().setModels(distinctModel(modelInfoList));
        } else {
            //1.构造配置参数对象

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            fileConfig.setSourceRootPath(sourceRootPath);
            newMeta.setFileConfig(fileConfig);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);

            fileInfoList.addAll(newFilesInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        //2.额外的输出配置
        if (templateMakerOutputConfig != null) {
            //文件外层和分组去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }

        //2. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);

        return id;
    }

    /**
     * 获取模型配置
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        //本次新增的模型列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();
        //非空校验
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        //处理模型信息
        //转换为配置文件接受的 modelInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());

        //如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            //复制变量
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            //模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            //不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成多个文件
     *
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        //非空校验
        List<Meta.FileConfig.FileInfo> newFilesInfoList = new ArrayList<>();
        if (templateMakerFileConfig == null) {
            return newFilesInfoList;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigsList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigsList)) {
            return newFilesInfoList;
        }
        //二 生成模板文件
        //遍历输入文件
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigsList) {
            String inputFilePath = fileInfoConfig.getPath();
            //如果是相对路径，要改为绝对路径
            if (!inputFilePath.startsWith(sourceRootPath)) {
                inputFilePath = sourceRootPath + File.separator + inputFilePath;
            }
            //传入绝对路径
            //获取过滤后的文件列表（不会存在目录）
            List<File> fileList = FileFilter.doFilter(inputFilePath, fileInfoConfig.getFilterConfigList());
            //不处理已经生成FTL的模板文件
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            //遍历文件列表
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFilesInfoList.add(fileInfo);
            }
        }


        //如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            //文件全放到一个分组内
            groupFileInfo.setFiles(newFilesInfoList);
            newFilesInfoList = new ArrayList<>();
            newFilesInfoList.add(groupFileInfo);
        }
        return newFilesInfoList;
    }

    /**
     * 制作模板文件
     *
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @param inputFile
     * @param fileInfoConfig
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath,
                                                             File inputFile,
                                                             TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String fileInputAbsolutePath = inputFile.getAbsolutePath();

        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\","/");
        // 要挖坑的文件(一定要的相对路径)
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";

        //二、使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        String fileContent;
        //如果已有模板文件，标识不是第一次制作，则在原有模板的基础上再挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }
        //支持多个模型：对于同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();

        //最新替换后的内容
        String newFileContent = fileContent;
        String replacement;

        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String filedName = modelInfoConfig.getFieldName();
            //模型配置
            //不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", filedName);
            } else {
                //是分组
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, filedName);
            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        //文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);
        //之前不存在目标文件，并且这次替换没有修改文件的内容，才是静态文件
        //和源文件一致，没有挖坑，静态生成
        if(!hasTemplateFile) {
            if (contentEquals) {
                //输出路径 = 输入路径
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                //输出模板文件
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        }else if (!contentEquals) {
            //有模板文件，并且增加了新坑，生成/更新模板文件
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }

        return fileInfo;
    }



    /**
     * 文件去重
     *
     * @param fileInfoList
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        //1.将所有文件配置（fileInfo）分为有分组和无分组

        //先处理分组的文件
        //{"groupKey":"a","files":[1,2]},{"groupKey":"a","files":[2,3]},{"groupKey":"b","files":[4,5]}
        //{"groupKey":"a","files":[[1,2],[2,3]]},{"groupKey":"b","files":[[4,5]]}
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );

        //2.对于有分组的文件配置，如果有相同的分组，同分组内的文件进行合并（merge），不同分组可同时保留

        //同组内配置合并
        //{"groupKey":"a","files":[[1,2],[2,3]]}
        //{"groupKey":"a","files":[[1,2,2,3]]}
        //{"groupKey":"a","files":[[1,2,3]]}
        //合并后的对象 map
        Map<String, Meta.FileConfig.FileInfo> groupKeyMeragedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            //[1,2,2,3]
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            //使用新的group配置
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);

            String groupKey = entry.getKey();
            groupKeyMeragedFileInfoMap.put(groupKey, newFileInfo);

        }

        //3.创建新的文件配置列表（结果列表），先将合并后的分组添加到结果列表
        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMeragedFileInfoMap.values());


        //4.再将无分组的文件配置列表添加到结果列表

        resultList.addAll(new ArrayList<>(fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values()));

        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModel(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        //1.将所有模型配置（modelInfo）分为有分组和无分组

        //先处理分组的模型
        //{"groupKey":"a","models":[1,2]},{"groupKey":"a","models":[2,3]},{"groupKey":"b","models":[4,5]}
        //{"groupKey":"a","models":[[1,2],[2,3]]},{"groupKey":"b","models":[[4,5]]}
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );

        //2.对于有分组的模型配置，如果有相同的分组，同分组内的模型进行合并（merge），不同分组可同时保留

        //同组内配置合并
        //{"groupKey":"a","models":[[1,2],[2,3]]}
        //{"groupKey":"a","models":[[1,2,2,3]]}
        //{"groupKey":"a","models":[[1,2,3]]}
        //合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMeragedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            //[1,2,2,3]
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            //使用新的group配置
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);

            String groupKey = entry.getKey();
            groupKeyMeragedModelInfoMap.put(groupKey, newModelInfo);

        }

        //3.创建新的模型配置列表（结果列表），先将合并后的分组添加到结果列表
        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMeragedModelInfoMap.values());


        //4.再将无分组的模型配置列表添加到结果列表

        resultList.addAll(new ArrayList<>(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));

        return resultList;
    }


}
