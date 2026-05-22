package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.event.TaskAssignedEvent;
import com.sme.pm.event.TaskStatusChangedEvent;
import com.sme.pm.event.TaskDependencyBlockedEvent;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.ITaskAttachmentService;
import com.sme.pm.service.ITaskCommentService;
import com.sme.pm.service.ITaskDependencyService;
import com.sme.pm.service.TaskService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int MAX_DEPTH = 4;
    private final TaskMapper taskMapper;
    private final TaskStatusMapper taskStatusMapper;
    private final ITaskCommentService taskCommentService;
    private final ITaskAttachmentService taskAttachmentService;
    private final ITaskDependencyService taskDependencyService;
    private final ApplicationEventPublisher eventPublisher;

    public TaskServiceImpl(TaskMapper taskMapper,
                          TaskStatusMapper taskStatusMapper,
                          ITaskCommentService taskCommentService,
                          ITaskAttachmentService taskAttachmentService,
                          ITaskDependencyService taskDependencyService,
                          ApplicationEventPublisher eventPublisher) {
        this.taskMapper = taskMapper;
        this.taskStatusMapper = taskStatusMapper;
        this.taskCommentService = taskCommentService;
        this.taskAttachmentService = taskAttachmentService;
        this.taskDependencyService = taskDependencyService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Task create(Task task) {
        // Generate task_id if not provided
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            int count = taskMapper.countAll();
            task.setTaskId(String.format("TSK%03d", count + 1));
        } else {
            // Check for duplicate task_id
            Task existing = taskMapper.findByTaskId(task.getTaskId());
            if (existing != null) {
                throw new IllegalArgumentException("任务ID已存在: " + task.getTaskId());
            }
        }

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

        if (task.getProgress() == null) {
            task.setProgress(0);
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
    public List<Task> listByProject(String projectId) {
        return taskMapper.findByProjectId(projectId);
    }

    @Override
    public List<Task> listByParent(Long parentId) {
        return taskMapper.findByParentId(parentId);
    }

    @Override
    public List<Task> listByAssignee(Long assigneeId) {
        return taskMapper.findByAssigneeId(assigneeId);
    }

    @Override
    public void assign(Long taskId, Long userId) {
        Task task = taskMapper.findById(taskId);
        if (task != null) {
            task.setAssigneeId(userId);
            taskMapper.updateById(task);

            // Publish TaskAssignedEvent
            TaskAssignedEvent event = new TaskAssignedEvent(
                    this,
                    userId,
                    taskId,
                    userId,
                    "Task Assigned",
                    "You have been assigned to task: " + task.getTitle(),
                    task.getProjectId()
            );
            eventPublisher.publishEvent(event);
        }
    }

    @Override
    public Task updateStatus(Long taskId, Long statusId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        TaskStatus newStatus = taskStatusMapper.selectById(statusId);
        if (newStatus == null) {
            throw new IllegalArgumentException("Status not found: " + statusId);
        }

        TaskStatus currentStatus = taskStatusMapper.selectById(task.getStatus());
        if (currentStatus != null && "DONE".equals(currentStatus.getCategory()) && !"DONE".equals(newStatus.getCategory())) {
            task.setCompletionDate(null);
            task.setInProgressSince(LocalDateTime.now());
        } else if (!"DONE".equals(currentStatus.getCategory()) && "DONE".equals(newStatus.getCategory())) {
            task.setCompletionDate(LocalDateTime.now());
            task.setInProgressSince(null);
            task.setProgress(100);
        } else if ("IN_PROGRESS".equals(newStatus.getCategory()) && task.getInProgressSince() == null) {
            task.setInProgressSince(LocalDateTime.now());
        }

        Long oldStatusId = task.getStatus() != null ? task.getStatus().longValue() : null;
        task.setStatus(statusId.intValue());
        taskMapper.updateById(task);

        // Publish TaskStatusChangedEvent
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
                this,
                task.getAssigneeId(),
                taskId,
                oldStatusId,
                statusId,
                "Task Status Changed",
                "Task \"" + task.getTitle() + "\" status changed to " + newStatus.getName(),
                task.getProjectId()
        );
        eventPublisher.publishEvent(event);

        return task;
    }

    @Override
    public boolean canTransitionTo(Long taskId, Long targetStatusId) {
        return taskDependencyService.canTransitionTo(taskId, targetStatusId);
    }

    @Override
    public List<TaskComment> getComments(Long taskId) {
        return taskCommentService.findByTaskId(taskId);
    }

    @Override
    public void addComment(Long taskId, TaskComment comment) {
        comment.setTaskId(taskId);
        taskCommentService.save(comment);
    }

    @Override
    public List<TaskAttachment> getAttachments(Long taskId) {
        return taskAttachmentService.findByTaskId(taskId);
    }

    @Override
    public void addAttachment(Long taskId, TaskAttachment attachment) {
        attachment.setTaskId(taskId);
        taskAttachmentService.uploadAttachment(attachment);
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        taskAttachmentService.deleteAttachment(attachmentId);
    }

    @Override
    public List<TaskDependency> getDependencies(Long taskId) {
        return taskDependencyService.findByTaskId(taskId);
    }

    @Override
    public List<TaskDependency> getBlockingDependencies(Long taskId) {
        return taskDependencyService.findByDependsOnTaskId(taskId);
    }

    @Override
    public void addDependency(Long taskId, TaskDependency dependency) {
        dependency.setTaskId(taskId);
        taskDependencyService.save(dependency);

        // Check for blocking dependencies and notify their assignees
        List<TaskDependency> blockingDeps = taskDependencyService.findByDependsOnTaskId(taskId);
        Task task = taskMapper.findById(taskId);
        for (TaskDependency blockingDep : blockingDeps) {
            Task blockingTask = taskMapper.findById(blockingDep.getTaskId());
            if (blockingTask != null && blockingTask.getAssigneeId() != null) {
                TaskDependencyBlockedEvent event = new TaskDependencyBlockedEvent(
                        this,
                        blockingTask.getAssigneeId(),
                        "Task Dependency Blocked",
                        "Task \"" + blockingTask.getTitle() + "\" is blocked by a dependency chain",
                        blockingTask.getId(),
                        blockingTask.getProjectId()
                );
                eventPublisher.publishEvent(event);
            }
        }
    }

    @Override
    public void removeDependency(Long dependencyId) {
        taskDependencyService.removeById(dependencyId);
    }

    @Override
    public int countBlockingDependencies(Long taskId) {
        return taskDependencyService.countBlockingDependencies(taskId);
    }
}
