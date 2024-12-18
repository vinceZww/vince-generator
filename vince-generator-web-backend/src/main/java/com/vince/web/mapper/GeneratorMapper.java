package com.vince.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vince.web.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Vince
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2024-12-10 07:57:08
 * @Entity com.vince.web.model.entity.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {

    @Select("SELECT id, distPath FROM generator WHERE isDelete=1")
    List<Generator> listDeletedGenerator();


}




