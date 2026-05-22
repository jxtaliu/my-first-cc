package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.TaskStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskStatusMapper extends BaseMapper<TaskStatus> {

    @Select("SELECT * FROM task_status WHERE (project_id = #{projectId} OR project_id IS NULL) AND deleted = 0 ORDER BY sort_order")
    List<TaskStatus> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM task_status WHERE project_id IS NULL AND deleted = 0 ORDER BY sort_order")
    List<TaskStatus> findSystemDefaults();

    @Select("SELECT * FROM task_status WHERE id = #{id} AND deleted = 0")
    TaskStatus findById(@Param("id") Long id);

    @Select("SELECT * FROM task_status WHERE code = #{code} AND deleted = 0 LIMIT 1")
    TaskStatus findByCode(@Param("code") String code);
}
