package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.TaskAttachment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskAttachmentMapper extends BaseMapper<TaskAttachment> {

    @Select("SELECT * FROM task_attachment WHERE task_id = #{taskId} AND deleted = 0 ORDER BY created_at DESC")
    List<TaskAttachment> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT * FROM task_attachment WHERE id = #{id} AND deleted = 0")
    TaskAttachment findById(@Param("id") Long id);
}
