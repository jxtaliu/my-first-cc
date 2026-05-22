package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.ProjectTemplate;
import com.sme.pm.service.IProjectTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project-templates")
public class ProjectTemplateController {

    private final IProjectTemplateService projectTemplateService;

    public ProjectTemplateController(IProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
    }

    @GetMapping
    public Result<List<ProjectTemplate>> findAll() {
        return Result.success(projectTemplateService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ProjectTemplate> findById(@PathVariable Long id) {
        ProjectTemplate template = projectTemplateService.findById(id);
        return template != null ? Result.success(template) : Result.error("Template not found");
    }

    @PostMapping
    public Result<ProjectTemplate> create(@RequestBody ProjectTemplate template) {
        projectTemplateService.save(template);
        return Result.success(template);
    }

    @PutMapping("/{id}")
    public Result<ProjectTemplate> update(@PathVariable Long id, @RequestBody ProjectTemplate template) {
        template.setId(id);
        projectTemplateService.updateById(template);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectTemplateService.removeById(id);
        return Result.success();
    }
}
