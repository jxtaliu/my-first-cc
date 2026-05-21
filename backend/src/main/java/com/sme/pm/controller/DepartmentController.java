package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Department;
import com.sme.pm.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Result<List<Department>> getAll() {
        return Result.success(departmentService.getAllDepartments());
    }

    @GetMapping("/tree")
    public Result<List<Department>> getTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        return dept == null ? Result.success(null) : Result.success(dept);
    }

    @PostMapping
    public Result<Department> create(@RequestBody Department department) {
        return Result.success(departmentService.create(department));
    }

    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentService.update(department));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!departmentService.canDelete(id)) {
            return Result.error("Cannot delete: department has users or children");
        }
        departmentService.delete(id);
        return Result.success();
    }
}
