package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.ProjectTemplate;
import com.sme.pm.service.IProjectTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目模板控制器
 * 提供项目模板管理相关API，包括模板CRUD等
 *
 * 用法：
 * - GET /api/v1/project-templates - 获取所有项目模板
 * - GET /api/v1/project-templates/{id} - 获取模板详情
 * - POST /api/v1/project-templates - 创建项目模板
 * - PUT /api/v1/project-templates/{id} - 更新项目模板
 * - DELETE /api/v1/project-templates/{id} - 删除项目模板
 */
@RestController
@RequestMapping("/api/v1/project-templates")
public class ProjectTemplateController {

    private final IProjectTemplateService projectTemplateService;

    public ProjectTemplateController(IProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
    }

    /**
     * 获取所有项目模板
     * @return 项目模板列表
     */
    @GetMapping
    public Result<List<ProjectTemplate>> findAll() {
        return Result.success(projectTemplateService.findAll());
    }

    /**
     * 根据ID获取项目模板详情
     * @param id 模板ID
     * @return 项目模板信息
     */
    @GetMapping("/{id}")
    public Result<ProjectTemplate> findById(@PathVariable Long id) {
        ProjectTemplate template = projectTemplateService.findById(id);
        return template != null ? Result.success(template) : Result.error("Template not found");
    }

    /**
     * 创建项目模板
     * @param template 模板信息
     * @return 创建的模板信息
     */
    @PostMapping
    public Result<ProjectTemplate> create(@RequestBody ProjectTemplate template) {
        projectTemplateService.save(template);
        return Result.success(template);
    }

    /**
     * 更新项目模板
     * @param id 模板ID
     * @param template 更新后的模板信息
     * @return 更新后的模板信息
     */
    @PutMapping("/{id}")
    public Result<ProjectTemplate> update(@PathVariable Long id, @RequestBody ProjectTemplate template) {
        template.setId(id);
        projectTemplateService.updateById(template);
        return Result.success(template);
    }

    /**
     * 删除项目模板
     * @param id 模板ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectTemplateService.removeById(id);
        return Result.success();
    }
}
