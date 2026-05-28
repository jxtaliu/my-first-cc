package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Milestone;
import com.sme.pm.service.IMilestoneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 里程碑控制器
 * 提供里程碑管理相关API，包括里程碑CRUD、跨项目查询、即将到期查询等
 *
 * 用法：
 * - GET /api/v1/milestones - 获取所有里程碑
 * - GET /api/v1/milestones/project/{projectId} - 获取项目的里程碑
 * - GET /api/v1/milestones/due-soon - 获取即将到期的里程碑
 * - POST /api/v1/milestones - 创建里程碑
 * - PUT /api/v1/milestones/{id} - 更新里程碑
 * - PUT /api/v1/milestones/{id}/complete - 完成里程碑
 */
@RestController
@RequestMapping("/api/v1/milestones")
public class MilestoneController {

    private final IMilestoneService milestoneService;

    public MilestoneController(IMilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    /**
     * 获取所有里程碑
     * @return 里程碑列表
     */
    @GetMapping
    public Result<List<Milestone>> findAll() {
        return Result.success(milestoneService.findAll());
    }

    /**
     * 获取跨项目里程碑
     * @return 跨项目里程碑列表
     */
    @GetMapping("/cross-project")
    public Result<List<Milestone>> findCrossProject() {
        return Result.success(milestoneService.findCrossProject());
    }

    /**
     * 获取项目的里程碑列表
     * @param projectId 项目ID
     * @return 里程碑列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<Milestone>> findByProjectId(@PathVariable String projectId) {
        return Result.success(milestoneService.findByProjectId(projectId));
    }

    /**
     * 获取即将到期的里程碑
     * @param days 未来天数（默认7天）
     * @return 即将到期的里程碑列表
     */
    @GetMapping("/due-soon")
    public Result<List<Milestone>> findDueSoon(@RequestParam(defaultValue = "7") int days) {
        return Result.success(milestoneService.findDueSoon(days));
    }

    /**
     * 根据ID获取里程碑详情
     * @param id 里程碑ID
     * @return 里程碑信息
     */
    @GetMapping("/{id}")
    public Result<Milestone> findById(@PathVariable Long id) {
        Milestone milestone = milestoneService.getById(id);
        return milestone != null ? Result.success(milestone) : Result.error("Milestone not found");
    }

    /**
     * 创建里程碑
     * @param milestone 里程碑信息
     * @return 创建的里程碑信息
     */
    @PostMapping
    public Result<Milestone> create(@RequestBody Milestone milestone) {
        milestoneService.save(milestone);
        return Result.success(milestone);
    }

    /**
     * 更新里程碑
     * @param id 里程碑ID
     * @param milestone 更新后的里程碑信息
     * @return 更新后的里程碑信息
     */
    @PutMapping("/{id}")
    public Result<Milestone> update(@PathVariable Long id, @RequestBody Milestone milestone) {
        milestone.setId(id);
        milestoneService.updateById(milestone);
        return Result.success(milestone);
    }

    /**
     * 删除里程碑
     * @param id 里程碑ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        milestoneService.removeById(id);
        return Result.success();
    }

    /**
     * 完成里程碑
     * @param id 里程碑ID
     * @return 完成后的里程碑信息
     */
    @PutMapping("/{id}/complete")
    public Result<Milestone> complete(@PathVariable Long id) {
        milestoneService.completeMilestone(id);
        return Result.success(milestoneService.getById(id));
    }
}
