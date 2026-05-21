package com.sme.pm.service.impl;

import com.sme.pm.entity.Timesheet;
import com.sme.pm.entity.Project;
import com.sme.pm.mapper.TimesheetMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.TimesheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetMapper timesheetMapper;
    private final ProjectMapper projectMapper;

    public TimesheetServiceImpl(TimesheetMapper timesheetMapper, ProjectMapper projectMapper) {
        this.timesheetMapper = timesheetMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public Timesheet create(Timesheet timesheet) {
        Project project = projectMapper.findById(timesheet.getProjectId());
        if (project != null && project.getProjectType() == 1) {
            timesheet.setApprovalStatus(2);
        } else {
            timesheet.setApprovalStatus(1);
        }

        timesheetMapper.insert(timesheet);
        return timesheet;
    }

    @Override
    public Timesheet update(Timesheet timesheet) {
        timesheetMapper.updateById(timesheet);
        return timesheet;
    }

    @Override
    public void delete(Long id) {
        timesheetMapper.deleteById(id);
    }

    @Override
    public Timesheet getById(Long id) {
        return timesheetMapper.selectById(id);
    }

    @Override
    public List<Timesheet> listWeekly(Long userId, String startDate, String endDate) {
        return timesheetMapper.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Timesheet> listMonthly(Long userId, String startDate, String endDate) {
        return timesheetMapper.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Timesheet> listByUser(Long userId) {
        return timesheetMapper.selectList(null);
    }

    @Override
    public List<Timesheet> listProjectTimesheets(Long projectId, String startDate, String endDate) {
        return timesheetMapper.findByProjectAndDateRange(projectId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getStats(Long userId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHours", 0);
        stats.put("approvedHours", 0);
        stats.put("pendingHours", 0);
        stats.put("weekHours", 0);
        return stats;
    }

    @Override
    @Transactional
    public void approve(Long timesheetId, Long approverId) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(2);
        timesheet.setApproverId(approverId);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheetMapper.updateById(timesheet);
    }

    @Override
    @Transactional
    public void reject(Long timesheetId, Long approverId, String reason) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(3);
        timesheet.setApproverId(approverId);
        timesheet.setRejectionReason(reason);
        timesheetMapper.updateById(timesheet);
    }

    @Override
    @Transactional
    public void resubmit(Long timesheetId) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(1);
        timesheetMapper.updateById(timesheet);
    }
}
