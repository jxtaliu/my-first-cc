package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.event.TaskAssignedEvent;
import com.sme.pm.event.TaskStatusChangedEvent;
import com.sme.pm.event.TaskDependencyBlockedEvent;
import com.sme.pm.mapper.TaskIdSequenceMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int MAX_DEPTH = 5;
    private static final Map<String, String> TYPE_PREFIXES = Map.of(
            "EPIC", "EPI",
            "FEATURE", "FEA",
            "STORY", "STO",
            "TASK", "TSK",
            "SUBTASK", "SUB",
            "BUG", "BUG"
    );

    private final TaskMapper taskMapper;
    private final TaskStatusMapper taskStatusMapper;
    private final TaskIdSequenceMapper taskIdSequenceMapper;
    private final ITaskCommentService taskCommentService;
    private final ITaskAttachmentService taskAttachmentService;
    private final ITaskDependencyService taskDependencyService;
    private final ApplicationEventPublisher eventPublisher;

    public TaskServiceImpl(TaskMapper taskMapper,
                          TaskStatusMapper taskStatusMapper,
                          TaskIdSequenceMapper taskIdSequenceMapper,
                          ITaskCommentService taskCommentService,
                          ITaskAttachmentService taskAttachmentService,
                          ITaskDependencyService taskDependencyService,
                          ApplicationEventPublisher eventPublisher) {
        this.taskMapper = taskMapper;
        this.taskStatusMapper = taskStatusMapper;
        this.taskIdSequenceMapper = taskIdSequenceMapper;
        this.taskCommentService = taskCommentService;
        this.taskAttachmentService = taskAttachmentService;
        this.taskDependencyService = taskDependencyService;
        this.eventPublisher = eventPublisher;
    }

    // Generate task_id based on type prefix and sequence
    private String generateTaskId(String projectId, String type) {
        String prefix = TYPE_PREFIXES.getOrDefault(type, "TSK");
        // Ensure sequence row exists
        taskIdSequenceMapper.insertIfNotExists(projectId, type);
        // Increment and get next value
        taskIdSequenceMapper.incrementSeq(projectId, type);
        Integer seq = taskIdSequenceMapper.getCurrentSeq(projectId, type);
        return prefix + String.format("%03d", seq != null ? seq : 1);
    }

    @Override
    public Task create(Task task) {
        // Validate required fields - none of these can be null
        if (task.getProjectId() == null || task.getProjectId().isEmpty()) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (task.getType() == null || task.getType().isEmpty()) {
            throw new IllegalArgumentException("type不能为空");
        }
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("title不能为空");
        }

        // Generate task_id if not provided (using type-specific prefix and sequence)
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(generateTaskId(task.getProjectId(), task.getType()));
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

        // Set default status to TODO if not specified
        if (task.getStatus() == null) {
            task.setStatus("TODO");
        }

        // Use standard insert
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task update(Task task) {
        // Validate required fields
        if (task.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        if (task.getTitle() != null && task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("title不能为空字符串");
        }

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
        return taskMapper.findByProjectIdWithUser(projectId);
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
    public Task updateStatus(Long taskId, String targetStatusCode) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        TaskStatus newStatus = taskStatusMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskStatus>()
                .eq(TaskStatus::getProjectId, task.getProjectId())
                .eq(TaskStatus::getCode, targetStatusCode)
        );
        if (newStatus == null) {
            throw new IllegalArgumentException("Status not found: " + targetStatusCode);
        }

        String currentCode = task.getStatus();
        if ("DONE".equals(currentCode) && !"DONE".equals(newStatus.getCode())) {
            task.setCompletionDate(null);
            task.setInProgressSince(LocalDateTime.now());
        } else if (!"DONE".equals(currentCode) && "DONE".equals(newStatus.getCode())) {
            task.setCompletionDate(LocalDateTime.now());
            task.setInProgressSince(null);
            task.setProgress(100);
        } else if ("IN_PROGRESS".equals(newStatus.getCode()) && task.getInProgressSince() == null) {
            task.setInProgressSince(LocalDateTime.now());
        }

        String oldStatusCode = task.getStatus();
        task.setStatus(newStatus.getCode());
        taskMapper.updateById(task);

        // Publish TaskStatusChangedEvent
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
                this,
                task.getAssigneeId(),
                taskId,
                oldStatusCode,
                newStatus.getCode(),
                "Task Status Changed",
                "Task \"" + task.getTitle() + "\" status changed to " + (newStatus.getNameEn() != null ? newStatus.getNameEn() : newStatus.getCode()),
                task.getProjectId()
        );
        eventPublisher.publishEvent(event);

        return task;
    }

    @Override
    public boolean canTransitionTo(Long taskId, String targetStatusCode) {
        return taskDependencyService.canTransitionTo(taskId, targetStatusCode);
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

    @Override
    public int countByStatusId(Integer statusId) {
        return taskMapper.countByStatusId(statusId);
    }

    // ==================== Requirement Tree Methods ====================

    @Override
    public List<Task> getRequirementTree(String projectId) {
        // Get all requirements for the project (all types)
        List<Task> allTasks = taskMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .in(Task::getType, "EPIC", "FEATURE", "STORY", "TASK", "SUBTASK")
                .eq(Task::getDeleted, 0)
                .orderByAsc(Task::getId));

        // Build tree structure
        Map<Long, Task> taskMap = new java.util.HashMap<>();
        List<Task> rootTasks = new java.util.ArrayList<>();

        // First pass: put all tasks in map
        for (Task task : allTasks) {
            taskMap.put(task.getId(), task);
            // Initialize children list
            task.setChildren(new java.util.ArrayList<>());
        }

        // Second pass: build tree
        for (Task task : allTasks) {
            if (task.getParentId() == null) {
                rootTasks.add(task);
            } else {
                Task parent = taskMap.get(task.getParentId());
                if (parent != null) {
                    parent.getChildren().add(task);
                } else {
                    // Orphan task, treat as root
                    rootTasks.add(task);
                }
            }
        }

        return rootTasks;
    }

    @Override
    public List<Task> getRequirementChildren(Long parentId) {
        // Get direct children of a requirement
        return taskMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getParentId, parentId)
                .eq(Task::getDeleted, 0)
                .orderByAsc(Task::getId));
    }

    @Override
    public List<Task> getRequirementSubtree(Long parentId) {
        // Get all descendants recursively (simplified - depth-limited)
        List<Task> allTasks = new ArrayList<>();
        collectChildren(parentId, allTasks);
        return allTasks;
    }

    private void collectChildren(Long parentId, List<Task> result) {
        List<Task> children = getRequirementChildren(parentId);
        for (Task child : children) {
            result.add(child);
            // Continue recursively if not at max depth
            if (child.getDepth() < MAX_DEPTH) {
                collectChildren(child.getId(), result);
            }
        }
    }

    @Override
    public void moveRequirement(Long taskId, Long newParentId, Integer sortOrder) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        // Update parent and depth
        if (newParentId != null) {
            Task newParent = taskMapper.selectById(newParentId);
            if (newParent != null) {
                task.setParentId(newParentId);
                task.setDepth(newParent.getDepth() + 1);
            }
        } else {
            task.setParentId(null);
            task.setDepth(1);
        }

        taskMapper.updateById(task);
    }

    // ==================== Bug Methods ====================

    @Override
    public List<Task> getBugs(String projectId) {
        return taskMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .eq(Task::getType, "BUG")
                .eq(Task::getDeleted, 0)
                .orderByDesc(Task::getCreatedAt));
    }

    @Override
    public void updateBugStatus(Long taskId, Long bugStatusId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        task.setBugStatusId(bugStatusId);
        taskMapper.updateById(task);
    }
}
