package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.TaskDependency;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {

    @Select("SELECT * FROM task_dependency WHERE task_id = #{taskId} AND deleted = 0")
    List<TaskDependency> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM task_dependency WHERE depends_on_task_id = #{dependsOnTaskId} AND deleted = 0")
    List<TaskDependency> findByDependsOnTaskId(@Param("dependsOnTaskId") Long dependsOnTaskId);

    @Select("SELECT COUNT(*) FROM task_dependency td " +
            "INNER JOIN task t ON td.depends_on_task_id = t.id " +
            "WHERE td.task_id = #{taskId} AND t.status != 5 AND t.deleted = 0")
    int countBlockingDependencies(@Param("taskId") Long taskId);  // 5 = DONE
}
