package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.Result;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.service.IBugStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bug-statuses")
public class BugStatusController {

    private final IBugStatusService bugStatusService;

    public BugStatusController(IBugStatusService bugStatusService) {
        this.bugStatusService = bugStatusService;
    }

    @GetMapping("/project/{projectId}")
    @RequireProjectRole(memberOnly = true)
    public Result<List<BugStatus>> getByProjectId(@PathVariable String projectId) {
        return Result.success(bugStatusService.getByProjectId(projectId));
    }

    @GetMapping("/project/{projectId}/transitions/{fromStatus}")
    @RequireProjectRole(memberOnly = true)
    public Result<List<String>> getAllowedTransitions(
            @PathVariable String projectId,
            @PathVariable String fromStatus) {
        return Result.success(bugStatusService.getAllowedTransitions(projectId, fromStatus));
    }

    @GetMapping("/project/{projectId}/can-transition")
    @RequireProjectRole(memberOnly = true)
    public Result<Boolean> canTransition(
            @PathVariable String projectId,
            @RequestParam String fromStatus,
            @RequestParam String toStatus) {
        return Result.success(bugStatusService.canTransition(projectId, fromStatus, toStatus));
    }

    @PostMapping("/init/{projectId}")
    @RequireProjectRole(value = {"PROJECT_OWNER"}, operation = "init_bug_statuses")
    public Result<Void> initializeForProject(@PathVariable String projectId) {
        bugStatusService.initializeForProject(projectId);
        return Result.success();
    }
}
