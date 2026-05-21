package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int MAX_DEPTH = 4;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public Task create(Task task) {
        // Validate max depth
        if (task.getParentId() != null) {
            Task parent = taskMapper.findById(task.getParentId());
            if (parent != null) {
                int newDepth = parent.getDepth() + 1;
                if (newDepth > MAX_DEPTH) {
                    throw new IllegalArgumentException("Maximum task hierarchy depth is " + MAX_DEPTH);
                }
                task.setDepth(newDepth);
            }
        } else {
            task.setDepth(1);
        }

        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task update(Task task) {
        taskMapper.updateById(task);
        return task;
    }

    @Override
    public void delete(Long id) {
        taskMapper.deleteById(id);
    }

    @Override
    public Task getById(Long id) {
        return taskMapper.findById(id);
    }

    @Override
    public List<Task> listBySprint(Long sprintId) {
        return taskMapper.findBySprintId(sprintId);
    }

    @Override
    public List<Task> listByParent(Long parentId) {
        return taskMapper.findByParentId(parentId);
    }
}
