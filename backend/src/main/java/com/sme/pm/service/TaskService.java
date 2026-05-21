package com.sme.pm.service;

import com.sme.pm.entity.Task;
import java.util.List;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    void delete(Long id);
    Task getById(Long id);
    List<Task> listBySprint(Long sprintId);
    List<Task> listByParent(Long parentId);
}
