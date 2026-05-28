package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.service.IProjectStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 项目统计控制器
 * 提供项目统计相关API，包括项目统计、燃尽图、CFD、Heatmap、吞吐量等
 *
 * 用法：
 * - GET /api/v1/projects/{projectId}/stats - 获取项目统计信息
 * - GET /api/v1/projects/compare - 对比多个项目
 * - GET /api/v1/projects/{projectId}/throughput - 获取团队吞吐量
 * - GET /api/v1/sprints/{sprintId}/burndown - 获取燃尽图数据
 * - GET /api/v1/projects/{projectId}/cfd - 获取累积流图数据
 * - GET /api/v1/projects/{projectId}/heatmap - 获取热力图数据
 * - GET /api/v1/projects/{projectId}/milestones/progress - 获取里程碑进度
 */
@RestController
@RequestMapping("/api/v1")
public class ProjectStatsController {

    private final IProjectStatsService projectStatsService;

    public ProjectStatsController(IProjectStatsService projectStatsService) {
        this.projectStatsService = projectStatsService;
    }

    /**
     * 获取项目统计信息（KPI数据）
     * @param projectId 项目ID
     * @return 统计信息（任务总数、完成数、进度等）
     */
    @GetMapping("/projects/{projectId}/stats")
    public Result<Map<String, Object>> getProjectStats(@PathVariable String projectId) {
        return Result.success(projectStatsService.getProjectStats(projectId));
    }

    /**
     * 对比多个项目的统计数据
     * @param ids 项目ID列表（逗号分隔）
     * @return 多个项目的统计对比数据
     */
    @GetMapping("/projects/compare")
    public Result<List<Map<String, Object>>> compareProjects(@RequestParam String ids) {
        List<String> projectIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .toList();
        return Result.success(projectStatsService.compareProjects(projectIds));
    }

    /**
     * 获取团队吞吐量数据
     * @param projectId 项目ID
     * @param startDate 可选，开始日期
     * @param endDate 可选，结束日期
     * @return 吞吐量统计数据
     */
    @GetMapping("/projects/{projectId}/throughput")
    public Result<Map<String, Object>> getTeamThroughput(@PathVariable String projectId,
                                                         @RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        return Result.success(projectStatsService.getTeamThroughput(projectId, startDate, endDate));
    }

    /**
     * 获取冲刺燃尽图数据
     * @param sprintId 冲刺ID
     * @return 燃尽图数据（每日剩余工作量）
     */
    @GetMapping("/sprints/{sprintId}/burndown")
    public Result<Map<String, Object>> getBurndownData(@PathVariable Long sprintId) {
        return Result.success(projectStatsService.getBurndownData(sprintId));
    }

    /**
     * 获取项目累积流图数据（CFD）
     * @param projectId 项目ID
     * @return CFD数据（每日各状态任务数）
     */
    @GetMapping("/projects/{projectId}/cfd")
    public Result<List<Map<String, Object>>> getCfdData(@PathVariable String projectId) {
        return Result.success(projectStatsService.getCfdData(projectId));
    }

    /**
     * 获取项目热力图数据
     * @param projectId 项目ID
     * @return 热力图数据（按负责人和优先级统计）
     */
    @GetMapping("/projects/{projectId}/heatmap")
    public Result<List<Map<String, Object>>> getHeatmapData(@PathVariable String projectId) {
        return Result.success(projectStatsService.getHeatmapData(projectId));
    }

    /**
     * 获取项目里程碑进度
     * @param projectId 项目ID
     * @return 里程碑进度列表
     */
    @GetMapping("/projects/{projectId}/milestones/progress")
    public Result<List<Map<String, Object>>> getMilestoneProgress(@PathVariable String projectId) {
        return Result.success(projectStatsService.getMilestoneProgress(projectId));
    }
}
