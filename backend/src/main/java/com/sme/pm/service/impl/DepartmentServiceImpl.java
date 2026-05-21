package com.sme.pm.service.impl;

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
        return departmentMapper.selectList(null);
    }

    @Override
    public List<Department> getDepartmentTree() {
        List<Department> all = departmentMapper.selectList(null);
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
        departmentMapper.insert(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        departmentMapper.updateById(department);
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
