package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequireProjectRole(memberOnly = true)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "create_task")
    public Result<Task> create(@RequestBody Task task, @CurrentUser Long userId) {
        task.setAssigneeId(userId);
        return Result.success(taskService.create(task));
    }

    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) Long sprintId,
                                   @RequestParam(required = false) String projectId,
                                   @RequestParam(required = false) Long assigneeId) {
        if (sprintId != null) {
            return Result.success(taskService.listBySprint(sprintId));
        }
        if (projectId != null) {
            return Result.success(taskService.listByProject(projectId));
        }
        if (assigneeId != null) {
            return Result.success(taskService.listByAssignee(assigneeId));
        }
        return Result.error("Must provide sprintId, projectId, or assigneeId");
    }

    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PutMapping("/{id}")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "update_task")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.update(task));
    }

    @DeleteMapping("/{id}")
    @RequireProjectRole(value = {"PROJECT_OWNER"}, operation = "delete_task")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_task")
    public Result<Task> move(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.move(task));
    }

    @PutMapping("/batch-move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_task")
    public Result<Map<String, Object>> batchMove(@RequestBody Map<String, Object> request,
                                                  @RequestParam String projectId) {
        @SuppressWarnings("unchecked")
        List<Number> taskIdNumbers = (List<Number>) request.get("taskIds");
        List<Long> taskIds = taskIdNumbers.stream().map(Number::longValue).toList();
        String targetStatus = (String) request.get("targetStatus");
        Long sprintId = request.get("sprintId") != null ? ((Number) request.get("sprintId")).longValue() : null;
        return Result.success(taskService.batchMove(taskIds, targetStatus, sprintId));
    }

    @PutMapping("/{id}/assign")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "assign_task")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long userId) {
        taskService.assign(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "update_task_status")
    public Result<Task> updateStatus(@PathVariable Long id, @RequestParam String statusCode) {
        return Result.success(taskService.updateStatus(id, statusCode));
    }

    @GetMapping("/{id}/can-transition/{targetStatusCode}")
    public Result<Boolean> canTransitionTo(@PathVariable Long id, @PathVariable String targetStatusCode) {
        return Result.success(taskService.canTransitionTo(id, targetStatusCode));
    }

    @GetMapping("/{id}/comments")
    public Result<List<TaskComment>> getComments(@PathVariable Long id) {
        return Result.success(taskService.getComments(id));
    }

    @PostMapping("/{id}/comments")
    public Result<Void> addComment(@PathVariable Long id, @RequestBody TaskComment comment) {
        taskService.addComment(id, comment);
        return Result.success();
    }

    @GetMapping("/{id}/attachments")
    public Result<List<TaskAttachment>> getAttachments(@PathVariable Long id) {
        return Result.success(taskService.getAttachments(id));
    }

    @PostMapping("/{id}/attachments")
    public Result<Void> addAttachment(@PathVariable Long id, @RequestBody TaskAttachment attachment) {
        taskService.addAttachment(id, attachment);
        return Result.success();
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return Result.success();
    }

    @GetMapping("/{id}/dependencies")
    public Result<List<TaskDependency>> getDependencies(@PathVariable Long id) {
        return Result.success(taskService.getDependencies(id));
    }

    @GetMapping("/{id}/blocking")
    public Result<List<TaskDependency>> getBlockingDependencies(@PathVariable Long id) {
        return Result.success(taskService.getBlockingDependencies(id));
    }

    @GetMapping("/{id}/blocking-count")
    public Result<Integer> countBlockingDependencies(@PathVariable Long id) {
        return Result.success(taskService.countBlockingDependencies(id));
    }

    @PostMapping("/{id}/dependencies")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "add_dependency")
    public Result<Void> addDependency(@PathVariable Long id, @RequestBody TaskDependency dependency) {
        taskService.addDependency(id, dependency);
        return Result.success();
    }

    @DeleteMapping("/dependencies/{dependencyId}")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "remove_dependency")
    public Result<Void> removeDependency(@PathVariable Long dependencyId) {
        taskService.removeDependency(dependencyId);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> countByStatus(@RequestParam Long statusId) {
        long count = taskService.countByStatusId(statusId.intValue());
        return Result.success(count);
    }
}
