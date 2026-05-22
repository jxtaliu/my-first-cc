package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sme.pm.entity.Department;
import com.sme.pm.mapper.DepartmentMapper;
import com.sme.pm.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = departmentMapper.selectList(null);
        for (Department dept : departments) {
            dept.setMemberCount(departmentMapper.countUsersByDepartmentId(dept.getId()));
        }
        return departments;
    }

    @Override
    public List<Department> getDepartmentTree() {
        List<Department> all = departmentMapper.selectList(null);
        for (Department dept : all) {
            dept.setMemberCount(departmentMapper.countUsersByDepartmentId(dept.getId()));
        }
        List<Department> roots = all.stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());
        for (Department root : roots) {
            buildTree(root, all);
        }
        return roots;
    }

    private void buildTree(Department parent, List<Department> all) {
        List<Department> children = all.stream()
                .filter(d -> parent.getId().equals(d.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (Department child : children) {
            buildTree(child, all);
        }
    }

    @Override
    public Department getById(Long id) {
        return departmentMapper.selectById(id);
    }

    @Override
    public Department create(Department department) {
        // Generate next departmentId (e.g., DEPT001, DEPT002, ...)
        Long maxId = departmentMapper.getMaxDepartmentIdNumber();
        String departmentId = String.format("DEPT%03d", maxId + 1);
        department.setDepartmentId(departmentId);

        departmentMapper.insert(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        LambdaUpdateWrapper<Department> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Department::getId, department.getId());
        if (department.getDepartmentId() != null) {
            wrapper.set(Department::getDepartmentId, department.getDepartmentId());
        }
        if (department.getName() != null) {
            wrapper.set(Department::getName, department.getName());
        }
        if (department.getParentId() != null) {
            wrapper.set(Department::getParentId, department.getParentId());
        }
        if (department.getSortOrder() != null) {
            wrapper.set(Department::getSortOrder, department.getSortOrder());
        }
        if (department.getStatus() != null) {
            wrapper.set(Department::getStatus, department.getStatus());
        }
        departmentMapper.update(null, wrapper);
        return department;
    }

    @Override
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }

    @Override
    public boolean canDelete(Long id) {
        return departmentMapper.countUsersByDepartmentId(id) == 0
                && departmentMapper.countChildrenByDepartmentId(id) == 0;
    }

    @Override
    public List<Department> getChildren(Long parentId) {
        return departmentMapper.findByParentId(parentId);
    }
}
