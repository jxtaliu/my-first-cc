package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT * FROM task WHERE sprint_id = #{sprintId} AND deleted = 0 ORDER BY created_at")
    List<Task> findBySprintId(@Param("sprintId") Long sprintId);

    @Select("SELECT * FROM task WHERE parent_id = #{parentId} AND deleted = 0")
    List<Task> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM task WHERE id = #{id} AND deleted = 0")
    Task findById(@Param("id") Long id);

    @Select("SELECT * FROM task WHERE task_id = #{taskId} AND deleted = 0")
    Task findByTaskId(@Param("taskId") String taskId);

    @Select("SELECT COUNT(*) FROM task WHERE deleted = 0")
    int countAll();

    @Select("SELECT MAX(depth) FROM task WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxChildDepth(@Param("parentId") Long parentId);

    @Select("SELECT * FROM task WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at")
    List<Task> findByProjectId(@Param("projectId") String projectId);

    @Select("SELECT * FROM task WHERE assignee_id = #{assigneeId} AND deleted = 0 ORDER BY created_at")
    List<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId);
}
