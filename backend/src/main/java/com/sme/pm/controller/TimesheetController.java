package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.service.TimesheetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @PostMapping
    public Result<Timesheet> create(@RequestBody Timesheet timesheet, @CurrentUser Long userId) {
        timesheet.setUserId(userId);
        return Result.success(timesheetService.create(timesheet));
    }

    @GetMapping("/weekly")
    public Result<List<Timesheet>> weekly(@CurrentUser Long userId,
                                          @RequestParam String startDate,
                                          @RequestParam String endDate) {
        return Result.success(timesheetService.listWeekly(userId, startDate, endDate));
    }

    @GetMapping("/monthly")
    public Result<List<Timesheet>> monthly(@CurrentUser Long userId,
                                           @RequestParam String startDate,
                                           @RequestParam String endDate) {
        return Result.success(timesheetService.listMonthly(userId, startDate, endDate));
    }

    @GetMapping("/project/{projectId}")
    public Result<List<Timesheet>> projectTimesheets(@PathVariable Long projectId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return Result.success(timesheetService.listProjectTimesheets(projectId, startDate, endDate));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @CurrentUser Long userId) {
        timesheetService.approve(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @CurrentUser Long userId,
                               @RequestParam String reason) {
        timesheetService.reject(id, userId, reason);
        return Result.success();
    }

    @PostMapping("/{id}/resubmit")
    public Result<Void> resubmit(@PathVariable Long id) {
        timesheetService.resubmit(id);
        return Result.success();
    }
}
