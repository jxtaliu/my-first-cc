package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.service.IProjectStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProjectStatsController {

    private final IProjectStatsService projectStatsService;

    public ProjectStatsController(IProjectStatsService projectStatsService) {
        this.projectStatsService = projectStatsService;
    }

    @GetMapping("/projects/{projectId}/stats")
    public Result<Map<String, Object>> getProjectStats(@PathVariable Long projectId) {
        return Result.success(projectStatsService.getProjectStats(projectId));
    }

    @GetMapping("/projects/compare")
    public Result<List<Map<String, Object>>> compareProjects(@RequestParam String ids) {
        List<Long> projectIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
        return Result.success(projectStatsService.compareProjects(projectIds));
    }

    @GetMapping("/projects/{projectId}/throughput")
    public Result<Map<String, Object>> getTeamThroughput(@PathVariable Long projectId,
                                                         @RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        return Result.success(projectStatsService.getTeamThroughput(projectId, startDate, endDate));
    }

    @GetMapping("/sprints/{sprintId}/burndown")
    public Result<Map<String, Object>> getBurndownData(@PathVariable Long sprintId) {
        return Result.success(projectStatsService.getBurndownData(sprintId));
    }

    @GetMapping("/projects/{projectId}/cfd")
    public Result<List<Map<String, Object>>> getCfdData(@PathVariable Long projectId) {
        return Result.success(projectStatsService.getCfdData(projectId));
    }

    @GetMapping("/projects/{projectId}/heatmap")
    public Result<List<Map<String, Object>>> getHeatmapData(@PathVariable Long projectId) {
        return Result.success(projectStatsService.getHeatmapData(projectId));
    }

    @GetMapping("/projects/{projectId}/milestones/progress")
    public Result<List<Map<String, Object>>> getMilestoneProgress(@PathVariable Long projectId) {
        return Result.success(projectStatsService.getMilestoneProgress(projectId));
    }
}
