package com.sme.pm.service;

import com.sme.pm.entity.Timesheet;
import java.util.List;
import java.util.Map;

public interface TimesheetService {
    Timesheet create(Timesheet timesheet);
    Timesheet update(Timesheet timesheet);
    void delete(Long id);
    Timesheet getById(Long id);
    List<Timesheet> listWeekly(Long userId, String startDate, String endDate);
    List<Timesheet> listMonthly(Long userId, String startDate, String endDate);
    List<Timesheet> listByUser(Long userId);
    List<Timesheet> listProjectTimesheets(String projectId, String startDate, String endDate);
    Map<String, Object> getStats(Long userId, String startDate, String endDate);
    List<Timesheet> listPendingByProject(String projectId);
    void approve(Long timesheetId, Long approverId);
    void reject(Long timesheetId, Long approverId, String reason);
    void resubmit(Long timesheetId);
}
