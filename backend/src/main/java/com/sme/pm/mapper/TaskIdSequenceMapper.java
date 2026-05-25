package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.TaskIdSequence;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TaskIdSequenceMapper extends BaseMapper<TaskIdSequence> {

    @Select("SELECT current_seq FROM task_id_sequence WHERE project_id = #{projectId} AND type = #{type}")
    Integer getCurrentSeq(@Param("projectId") String projectId, @Param("type") String type);

    @Insert("INSERT IGNORE INTO task_id_sequence (project_id, type, current_seq) VALUES (#{projectId}, #{type}, 0)")
    int insertIfNotExists(@Param("projectId") String projectId, @Param("type") String type);

    @Update("UPDATE task_id_sequence SET current_seq = current_seq + 1 WHERE project_id = #{projectId} AND type = #{type}")
    int incrementSeq(@Param("projectId") String projectId, @Param("type") String type);
}

