package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public Task move(Task task) {
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
    public List<Task> listByProject(Long projectId) {
        return new ArrayList<>();
    }

    @Override
    public List<Task> listByParent(Long parentId) {
        return taskMapper.findByParentId(parentId);
    }

    @Override
    public void assign(Long taskId, Long userId) {
        Task task = taskMapper.findById(taskId);
        if (task != null) {
            task.setAssigneeId(userId);
            taskMapper.updateById(task);
        }
    }

    @Override
    public List<Object> getComments(Long taskId) {
        return new ArrayList<>();
    }

    @Override
    public void addComment(Long taskId, Object comment) {
        // Placeholder - comments not implemented yet
    }
}
