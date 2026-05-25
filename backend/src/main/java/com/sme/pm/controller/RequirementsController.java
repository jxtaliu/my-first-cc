package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/requirements")
@RequireProjectRole(memberOnly = true)
public class RequirementsController {

    private final TaskService taskService;

    public RequirementsController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get requirement tree (Epic and Feature only - top level)
     */
    @GetMapping("/tree")
    public Result<List<Task>> getRequirementTree(@RequestParam String projectId) {
        return Result.success(taskService.getRequirementTree(projectId));
    }

    /**
     * Get direct children of a requirement
     */
    @GetMapping("/{id}/children")
    public Result<List<Task>> getRequirementChildren(@PathVariable Long id) {
        return Result.success(taskService.getRequirementChildren(id));
    }

    /**
     * Get full subtree of a requirement
     */
    @GetMapping("/{id}/subtree")
    public Result<List<Task>> getRequirementSubtree(@PathVariable Long id) {
        return Result.success(taskService.getRequirementSubtree(id));
    }

    /**
     * Move/ reorder a requirement - requires PROJECT_OWNER or PROJECT_MANAGER
     */
    @PutMapping("/move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_requirement")
    public Result<Void> moveRequirement(@RequestBody MoveRequirementRequest request) {
        taskService.moveRequirement(request.getTaskId(), request.getNewParentId(), request.getSortOrder());
        return Result.success();
    }

    public static class MoveRequirementRequest {
        private Long taskId;
        private Long newParentId;
        private Integer sortOrder;

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Long getNewParentId() { return newParentId; }
        public void setNewParentId(Long newParentId) { this.newParentId = newParentId; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
