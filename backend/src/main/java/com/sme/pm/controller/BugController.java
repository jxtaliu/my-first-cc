package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.service.IBugStatusService;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/bugs")
public class BugController {

    private final TaskService taskService;
    private final IBugStatusService bugStatusService;

    public BugController(TaskService taskService, IBugStatusService bugStatusService) {
        this.taskService = taskService;
        this.bugStatusService = bugStatusService;
    }

    /**
     * Get bugs for a project - requires project membership
     */
    @GetMapping
    @RequireProjectRole(memberOnly = true)
    public Result<List<Task>> getBugs(@RequestParam String projectId) {
        return Result.success(taskService.getBugs(projectId));
    }

    /**
     * Create a bug - requires PROJECT_OWNER, PROJECT_MANAGER or DEVELOPER role
     */
    @PostMapping
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "create_bug")
    public Result<Task> createBug(@RequestBody Task bug, @CurrentUser Long userId) {
        bug.setType("BUG");
        bug.setAssigneeId(userId);
        return Result.success(taskService.create(bug));
    }

    /**
     * Get bug status options for a project - public endpoint, no auth needed
     */
    @GetMapping("/statuses/{projectId}")
    public Result<List<BugStatus>> getBugStatuses(@PathVariable String projectId) {
        return Result.success(bugStatusService.getByProjectId(projectId));
    }

    /**
     * Get allowed status transitions for a bug - public endpoint, no auth needed
     */
    @GetMapping("/transitions/{projectId}/{fromStatus}")
    public Result<List<String>> getAllowedTransitions(
            @PathVariable String projectId,
            @PathVariable String fromStatus) {
        return Result.success(bugStatusService.getAllowedTransitions(projectId, fromStatus));
    }

    /**
     * Update bug status - requires PROJECT_OWNER or PROJECT_MANAGER role
     */
    @PutMapping("/{id}/status")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "update_bug_status")
    public Result<Void> updateBugStatus(
            @PathVariable Long id,
            @RequestParam Long bugStatusId) {
        taskService.updateBugStatus(id, bugStatusId);
        return Result.success();
    }
}
