package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Timesheet;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimesheetMapper extends BaseMapper<Timesheet> {

    @Select("SELECT * FROM timesheet WHERE user_id = #{userId} AND deleted = 0 " +
            "AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date")
    List<Timesheet> findByUserAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    @Select("SELECT * FROM timesheet WHERE project_id = #{projectId} AND deleted = 0 " +
            "AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date")
    List<Timesheet> findByProjectAndDateRange(@Param("projectId") Long projectId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    @Select("SELECT * FROM timesheet WHERE approval_status = 1 AND deleted = 0")
    List<Timesheet> findPendingApproval();

    @Select("SELECT * FROM timesheet WHERE project_id = #{projectId} AND approval_status = 1 AND deleted = 0 ORDER BY created_at DESC")
    List<Timesheet> findPendingByProject(@Param("projectId") Long projectId);

    @Select("SELECT * FROM timesheet WHERE user_id = #{userId} AND deleted = 0 ORDER BY work_date DESC")
    List<Timesheet> findByUserId(@Param("userId") Long userId);
}
