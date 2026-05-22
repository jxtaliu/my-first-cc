package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.ProjectTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectTemplateMapper extends BaseMapper<ProjectTemplate> {

    @Select("SELECT * FROM project_template WHERE deleted = 0 ORDER BY created_at")
    List<ProjectTemplate> findAll();

    @Select("SELECT * FROM project_template WHERE id = #{id} AND deleted = 0")
    ProjectTemplate findById(@Param("id") Long id);
}
