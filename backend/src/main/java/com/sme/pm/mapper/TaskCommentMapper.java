package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.TaskComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {

    @Select("SELECT * FROM task_comment WHERE task_id = #{taskId} AND deleted = 0 ORDER BY created_at")
    List<TaskComment> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM task_comment WHERE parent_comment_id = #{parentId} AND deleted = 0 ORDER BY created_at")
    List<TaskComment> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM task_comment WHERE id = #{id} AND deleted = 0")
    TaskComment findById(@Param("id") Long id);
}
