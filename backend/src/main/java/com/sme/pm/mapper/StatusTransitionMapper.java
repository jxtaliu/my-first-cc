package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.StatusTransition;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StatusTransitionMapper extends BaseMapper<StatusTransition> {

    @Select("SELECT * FROM status_transition WHERE (project_id = #{projectId} OR project_id IS NULL) AND deleted = 0")
    List<StatusTransition> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM status_transition WHERE from_status_id = #{fromStatusId} AND deleted = 0")
    List<StatusTransition> findByFromStatus(@Param("fromStatusId") Long fromStatusId);

    @Select("SELECT COUNT(*) FROM status_transition WHERE from_status_id = #{fromStatusId} AND to_status_id = #{toStatusId} AND deleted = 0")
    int existsTransition(@Param("fromStatusId") Long fromStatusId, @Param("toStatusId") Long toStatusId);
}
