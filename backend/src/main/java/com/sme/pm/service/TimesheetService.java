package com.sme.pm.service;

import com.sme.pm.entity.Timesheet;
import java.util.List;

public interface TimesheetService {
    Timesheet create(Timesheet timesheet);
    Timesheet update(Timesheet timesheet);
    void delete(Long id);
    List<Timesheet> listWeekly(Long userId, String startDate, String endDate);
    List<Timesheet> listMonthly(Long userId, String startDate, String endDate);
    List<Timesheet> listProjectTimesheets(Long projectId, String startDate, String endDate);
    void approve(Long timesheetId, Long approverId);
    void reject(Long timesheetId, Long approverId, String reason);
    void resubmit(Long timesheetId);
}
