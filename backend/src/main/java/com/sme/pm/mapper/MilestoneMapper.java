package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Milestone;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MilestoneMapper extends BaseMapper<Milestone> {

    @Select("SELECT * FROM milestone WHERE deleted = 0 ORDER BY target_date")
    List<Milestone> findAll();

    @Select("SELECT * FROM milestone WHERE is_cross_project = 1 AND deleted = 0 ORDER BY target_date")
    List<Milestone> findCrossProject();

    @Select("SELECT m.* FROM milestone m " +
            "INNER JOIN project_milestone pm ON m.id = pm.milestone_id " +
            "WHERE pm.project_id = #{projectId} AND m.deleted = 0 " +
            "ORDER BY m.target_date")
    List<Milestone> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM milestone WHERE target_date <= #{endDate} AND status = 'ACTIVE' AND deleted = 0")
    List<Milestone> findDueSoon(@Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM milestone WHERE id = #{id} AND deleted = 0")
    Milestone findById(@Param("id") Long id);
}
