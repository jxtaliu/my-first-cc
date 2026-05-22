package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.ProjectRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectRoleMapper extends BaseMapper<ProjectRole> {

    @Select("SELECT * FROM project_role WHERE project_id = #{projectId} AND deleted = 0")
    List<ProjectRole> findByProjectId(@Param("projectId") String projectId);

    @Select("SELECT * FROM project_role WHERE user_id = #{userId} AND deleted = 0")
    List<ProjectRole> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM project_role WHERE project_id = #{projectId} AND user_id = #{userId} AND deleted = 0")
    ProjectRole findByProjectAndUser(@Param("projectId") String projectId, @Param("userId") Long userId);

    @Select("SELECT * FROM project_role WHERE project_id = #{projectId} AND role = #{role} AND deleted = 0")
    List<ProjectRole> findByProjectAndRole(@Param("projectId") String projectId, @Param("role") String role);

    @Delete("DELETE FROM project_role WHERE project_id = #{projectId} AND user_id = #{userId}")
    void deleteByProjectAndUser(@Param("projectId") String projectId, @Param("userId") Long userId);
}
