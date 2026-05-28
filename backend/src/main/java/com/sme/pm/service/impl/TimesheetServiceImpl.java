package com.sme.pm.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sme.pm.entity.Timesheet;
import com.sme.pm.entity.Project;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.event.TimesheetApprovalEvent;
import com.sme.pm.event.TimesheetSubmittedEvent;
import com.sme.pm.mapper.TimesheetMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.IProjectRoleService;
import com.sme.pm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimesheetServiceImpl extends ServiceImpl<TimesheetMapper, Timesheet> implements TimesheetService {

    @Autowired
    private TimesheetMapper timesheetMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private IProjectRoleService projectRoleService;

    @Override
    @Transactional
    public Timesheet create(Timesheet timesheet) {
        // Validate required fields
        if (timesheet.getUserId() == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (timesheet.getProjectId() == null || timesheet.getProjectId().isEmpty()) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (timesheet.getWorkDate() == null) {
            throw new IllegalArgumentException("workDate不能为空");
        }
        if (timesheet.getHours() == null) {
            throw new IllegalArgumentException("hours不能为空");
        }

        timesheet.setApprovalStatus(1);
        timesheetMapper.insert(timesheet);

        // Publish TimesheetSubmittedEvent to notify project managers
        TimesheetSubmittedEvent event = new TimesheetSubmittedEvent(
                this,
                timesheet.getUserId(),
                timesheet.getId(),
                "Timesheet Submitted",
                "A new timesheet requires your approval",
                timesheet.getProjectId()
        );
        eventPublisher.publishEvent(event);

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
        return timesheetMapper.findByUserId(userId);
    }

    @Override
    public List<Timesheet> listProjectTimesheets(String projectId, String startDate, String endDate) {
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
    public List<Timesheet> listPendingByProject(String projectId) {
        return timesheetMapper.findPendingByProject(projectId);
    }

    @Override
    @Transactional
    public void approve(Long timesheetId, Long approverId) {
        Timesheet timesheet = timesheetMapper.selectById(timesheetId);

        // Check if approver has PROJECT_MANAGER role in the project
        List<ProjectRole> pmRoles = projectRoleService.findByProjectAndRole(timesheet.getProjectId(), "PROJECT_MANAGER");
        boolean isProjectManager = pmRoles.stream().anyMatch(r -> r.getUserId().equals(approverId));
        if (!isProjectManager) {
            throw new IllegalStateException("Only project manager can approve timesheets");
        }

        timesheet.setApprovalStatus(2);
        timesheet.setApproverId(approverId);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheetMapper.updateById(timesheet);

        // Publish TimesheetApprovalEvent
        TimesheetApprovalEvent event = new TimesheetApprovalEvent(
                this,
                timesheet.getUserId(),
                timesheetId,
                "APPROVED",
                "Timesheet Approved",
                "Your timesheet has been approved",
                timesheet.getProjectId()
        );
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void reject(Long timesheetId, Long approverId, String reason) {
        Timesheet timesheet = timesheetMapper.selectById(timesheetId);

        // Check if approver has PROJECT_MANAGER role in the project
        List<ProjectRole> pmRoles = projectRoleService.findByProjectAndRole(timesheet.getProjectId(), "PROJECT_MANAGER");
        boolean isProjectManager = pmRoles.stream().anyMatch(r -> r.getUserId().equals(approverId));
        if (!isProjectManager) {
            throw new IllegalStateException("Only project manager can reject timesheets");
        }

        timesheet.setApprovalStatus(3);
        timesheet.setApproverId(approverId);
        timesheet.setRejectionReason(reason);
        timesheetMapper.updateById(timesheet);

        // Publish TimesheetApprovalEvent
        TimesheetApprovalEvent event = new TimesheetApprovalEvent(
                this,
                timesheet.getUserId(),
                timesheetId,
                "REJECTED",
                "Timesheet Rejected",
                "Your timesheet has been rejected: " + reason,
                timesheet.getProjectId()
        );
        eventPublisher.publishEvent(event);
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
