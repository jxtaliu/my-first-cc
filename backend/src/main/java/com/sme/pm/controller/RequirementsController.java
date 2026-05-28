package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求控制器
 * 提供需求（Epic、Feature、Story）管理相关API，包括需求树查询、子需求、需求移动等
 *
 * 用法：
 * - GET /api/v1/tasks/requirements/tree - 获取项目需求树（Epic和Feature）
 * - GET /api/v1/tasks/requirements/{id}/children - 获取需求的直接子需求
 * - GET /api/v1/tasks/requirements/{id}/subtree - 获取需求的完整子树
 * - PUT /api/v1/tasks/requirements/move - 移动/重排序需求
 */
@RestController
@RequestMapping("/api/v1/tasks/requirements")
@RequireProjectRole(memberOnly = true)
public class RequirementsController {

    private final TaskService taskService;

    public RequirementsController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 获取项目需求树（仅返回Epic和Feature作为顶层节点）
     * @param projectId 项目ID
     * @return 需求树结构（包含子节点层级）
     */
    @GetMapping("/tree")
    public Result<List<Task>> getRequirementTree(@RequestParam String projectId) {
        return Result.success(taskService.getRequirementTree(projectId));
    }

    /**
     * 获取需求的直接子需求
     * @param id 需求ID
     * @return 直接子需求列表
     */
    @GetMapping("/{id}/children")
    public Result<List<Task>> getRequirementChildren(@PathVariable Long id) {
        return Result.success(taskService.getRequirementChildren(id));
    }

    /**
     * 获取需求的完整子树（递归获取所有后代）
     * @param id 需求ID
     * @return 完整的需求子树
     */
    @GetMapping("/{id}/subtree")
    public Result<List<Task>> getRequirementSubtree(@PathVariable Long id) {
        return Result.success(taskService.getRequirementSubtree(id));
    }

    /**
     * 移动或重排序需求
     * @param request 包含taskId、newParentId、sortOrder的请求体
     * @return 成功返回空结果
     */
    @PutMapping("/move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_requirement")
    public Result<Void> moveRequirement(@RequestBody MoveRequirementRequest request) {
        taskService.moveRequirement(request.getTaskId(), request.getNewParentId(), request.getSortOrder());
        return Result.success();
    }

    /**
     * 移动需求请求体
     */
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
