package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.service.IRolePermissionService;
import com.sme.pm.service.TimesheetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工时记录控制器
 * 提供工时记录管理相关API，包括工时填报、审批、统计等
 *
 * 用法：
 * - POST /api/timesheets - 创建工时记录
 * - GET /api/timesheets/my - 获取当前用户的工时记录
 * - GET /api/timesheets/weekly - 获取周工时列表
 * - POST /api/timesheets/{id}/approve - 审批工时
 * - POST /api/timesheets/{id}/reject - 驳回工时
 * - GET /api/timesheets/pending-approval/{projectId} - 获取待审批工时列表
 */
@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final IRolePermissionService rolePermissionService;

    public TimesheetController(TimesheetService timesheetService, IRolePermissionService rolePermissionService) {
        this.timesheetService = timesheetService;
        this.rolePermissionService = rolePermissionService;
    }

    /**
     * 创建工时记录
     * @param timesheet 工时信息
     * @param userId 当前登录用户ID
     * @return 创建的工时记录
     */
    @PostMapping
    public Result<Timesheet> create(@RequestBody Timesheet timesheet, @CurrentUser Long userId) {
        timesheet.setUserId(userId);
        return Result.success(timesheetService.create(timesheet));
    }

    /**
     * 获取工时记录详情
     * @param id 工时记录ID
     * @return 工时记录信息
     */
    @GetMapping("/{id}")
    public Result<Timesheet> getById(@PathVariable Long id) {
        return Result.success(timesheetService.getById(id));
    }

    /**
     * 更新工时记录
     * @param id 工时记录ID
     * @param timesheet 更新后的工时信息
     * @return 更新后的工时记录
     */
    @PutMapping("/{id}")
    public Result<Timesheet> update(@PathVariable Long id, @RequestBody Timesheet timesheet) {
        timesheet.setId(id);
        return Result.success(timesheetService.update(timesheet));
    }

    /**
     * 删除工时记录
     * @param id 工时记录ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timesheetService.delete(id);
        return Result.success();
    }

    /**
     * 获取周工时列表
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 工时记录列表
     */
    @GetMapping("/weekly")
    public Result<List<Timesheet>> weekly(@CurrentUser Long userId,
                                          @RequestParam String startDate,
                                          @RequestParam String endDate) {
        return Result.success(timesheetService.listWeekly(userId, startDate, endDate));
    }

    /**
     * 获取月工时列表
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 工时记录列表
     */
    @GetMapping("/monthly")
    public Result<List<Timesheet>> monthly(@CurrentUser Long userId,
                                           @RequestParam String startDate,
                                           @RequestParam String endDate) {
        return Result.success(timesheetService.listMonthly(userId, startDate, endDate));
    }

    /**
     * 获取项目工时列表
     * @param projectId 项目ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工时记录列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<Timesheet>> projectTimesheets(@PathVariable String projectId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return Result.success(timesheetService.listProjectTimesheets(projectId, startDate, endDate));
    }

    /**
     * 获取当前用户的工时记录
     * @param userId 当前登录用户ID
     * @param startDate 可选，开始日期
     * @param endDate 可选，结束日期
     * @return 工时记录列表
     */
    @GetMapping("/my")
    public Result<List<Timesheet>> myTimesheets(@CurrentUser Long userId,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        if (startDate != null && endDate != null) {
            return Result.success(timesheetService.listWeekly(userId, startDate, endDate));
        }
        return Result.success(timesheetService.listByUser(userId));
    }

    /**
     * 获取工时统计信息
     * @param userId 用户ID
     * @param startDate 可选，开始日期
     * @param endDate 可选，结束日期
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<Object> stats(@CurrentUser Long userId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        return Result.success(timesheetService.getStats(userId, startDate, endDate));
    }

    /**
     * 审批工时记录
     * @param id 工时记录ID
     * @param userId 审批人ID（当前登录用户）
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @CurrentUser Long userId) {
        // Check permission - only PROJECT_MANAGER or PROJECT_OWNER can approve
        Timesheet timesheet = timesheetService.getById(id);
        if (timesheet == null) {
            return Result.error("Timesheet not found");
        }

        if (!rolePermissionService.canApproveTimesheet(userId, timesheet.getProjectId())) {
            return Result.error(403, "You don't have permission to approve timesheets for this project");
        }

        timesheetService.approve(id, userId);
        return Result.success();
    }

    /**
     * 驳回工时记录
     * @param id 工时记录ID
     * @param userId 审批人ID（当前登录用户）
     * @param reason 驳回原因
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @CurrentUser Long userId,
                               @RequestParam String reason) {
        // Check permission - only PROJECT_MANAGER or PROJECT_OWNER can reject
        Timesheet timesheet = timesheetService.getById(id);
        if (timesheet == null) {
            return Result.error("Timesheet not found");
        }

        if (!rolePermissionService.canApproveTimesheet(userId, timesheet.getProjectId())) {
            return Result.error(403, "You don't have permission to reject timesheets for this project");
        }

        timesheetService.reject(id, userId, reason);
        return Result.success();
    }

    /**
     * 获取项目待审批工时列表
     * @param projectId 项目ID
     * @return 待审批工时列表
     */
    @GetMapping("/pending-approval/{projectId}")
    public Result<List<Timesheet>> getPendingApproval(@PathVariable String projectId) {
        return Result.success(timesheetService.listPendingByProject(projectId));
    }

    /**
     * 重新提交工时记录
     * @param id 工时记录ID
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/resubmit")
    public Result<Void> resubmit(@PathVariable Long id) {
        timesheetService.resubmit(id);
        return Result.success();
    }
}
