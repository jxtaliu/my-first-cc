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

    @GetMapping("/{id}")
    public Result<Timesheet> getById(@PathVariable Long id) {
        return Result.success(timesheetService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Timesheet> update(@PathVariable Long id, @RequestBody Timesheet timesheet) {
        timesheet.setId(id);
        return Result.success(timesheetService.update(timesheet));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timesheetService.delete(id);
        return Result.success();
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

    @GetMapping("/my")
    public Result<List<Timesheet>> myTimesheets(@CurrentUser Long userId,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        if (startDate != null && endDate != null) {
            return Result.success(timesheetService.listWeekly(userId, startDate, endDate));
        }
        return Result.success(timesheetService.listByUser(userId));
    }

    @GetMapping("/stats")
    public Result<Object> stats(@CurrentUser Long userId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        return Result.success(timesheetService.getStats(userId, startDate, endDate));
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
