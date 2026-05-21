package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Sprint;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SprintMapper extends BaseMapper<Sprint> {

    @Select("SELECT * FROM sprint WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<Sprint> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM sprint WHERE id = #{id} AND deleted = 0")
    Sprint findById(@Param("id") Long id);
}
