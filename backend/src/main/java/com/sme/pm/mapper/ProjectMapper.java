package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT * FROM project WHERE deleted = 0 ORDER BY created_at DESC")
    List<Project> findAll();

    @Select("SELECT * FROM project WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Project> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM project WHERE id = #{id} AND deleted = 0")
    Project findById(@Param("id") Long id);

    @Select("SELECT * FROM project WHERE project_id = #{projectId} AND deleted = 0")
    Project findByProjectId(@Param("projectId") String projectId);

    @Select("SELECT COUNT(*) FROM project WHERE deleted = 0")
    int countAll();

    @Insert("INSERT INTO project_member (project_id, user_id, joined_at) VALUES (#{projectId}, #{userId}, NOW())")
    void addMember(@Param("projectId") String projectId, @Param("userId") Long userId);

    @Delete("DELETE FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    void removeMember(@Param("projectId") String projectId, @Param("userId") Long userId);

    @Select("SELECT user_id FROM project_member WHERE project_id = #{projectId}")
    List<Long> findMemberIds(@Param("projectId") String projectId);
}
