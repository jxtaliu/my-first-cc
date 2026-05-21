package com.sme.pm.service;

import com.sme.pm.entity.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    List<Department> getDepartmentTree();
    Department getById(Long id);
    Department create(Department department);
    Department update(Department department);
    void delete(Long id);
    boolean canDelete(Long id);
    List<Department> getChildren(Long parentId);
}
