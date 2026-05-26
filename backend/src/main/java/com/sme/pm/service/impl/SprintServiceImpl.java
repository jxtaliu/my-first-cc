package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ISprintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SprintServiceImpl extends ServiceImpl<SprintMapper, Sprint> implements ISprintService {

    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;

    public SprintServiceImpl(SprintMapper sprintMapper, TaskMapper taskMapper, ProjectMapper projectMapper) {
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<Sprint> findByProjectId(String projectId) {
        return sprintMapper.findByProjectId(projectId);
    }

    @Override
    public Sprint findById(Long id) {
        return sprintMapper.findById(id);
    }

    @Override
    @Transactional
    public Sprint create(Sprint sprint) {
        // Validate required fields
        if (sprint.getProjectId() == null || sprint.getProjectId().isEmpty()) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (sprint.getName() == null || sprint.getName().isEmpty()) {
            throw new IllegalArgumentException("name不能为空");
        }

        sprint.setStatus("PLANNING");
        sprintMapper.insert(sprint);
        return sprint;
    }

    @Override
    @Transactional
    public Sprint update(Sprint sprint) {
        sprintMapper.updateById(sprint);
        return sprint;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sprintMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Sprint startSprint(Long id) {
        Sprint sprint = sprintMapper.findById(id);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint not found");
        }
        sprint.setStatus("ACTIVE");
        sprint.setStartDate(LocalDate.now());
        sprintMapper.updateById(sprint);
        return sprint;
    }

    @Override
    @Transactional
    public Sprint completeSprint(Long id) {
        Sprint sprint = sprintMapper.findById(id);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint not found");
        }
        sprint.setStatus("COMPLETED");
        sprint.setEndDate(LocalDate.now());
        sprintMapper.updateById(sprint);
        return sprint;
    }

    @Override
    public int calculateVelocity(Long sprintId) {
        List<Task> tasks = taskMapper.findBySprintId(sprintId);
        int velocity = 0;
        for (Task task : tasks) {
            // Sum up story points from progress or estimate hours
            if (task.getProgress() != null && task.getProgress() == 100) {
                // Completed task - use estimateHours as story points proxy
                velocity += (task.getEstimateHours() != null) ? task.getEstimateHours() : 0;
            }
        }
        return velocity;
    }

    @Override
    public int calculateCapacity(Long sprintId) {
        Sprint sprint = sprintMapper.findById(sprintId);
        if (sprint == null) {
            return 0;
        }
        return sprint.getCapacityHours() != null ? sprint.getCapacityHours() : 0;
    }

    @Override
    public Map<String, Object> getSprintStats(Long sprintId) {
        Map<String, Object> stats = new HashMap<>();

        List<Task> tasks = taskMapper.findBySprintId(sprintId);
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int remainingTasks = 0;

        for (Task task : tasks) {
            if (task.getProgress() != null && task.getProgress() == 100) {
                completedTasks++;
            } else {
                remainingTasks++;
            }
        }

        int velocity = calculateVelocity(sprintId);
        int capacity = calculateCapacity(sprintId);

        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("remainingTasks", remainingTasks);
        stats.put("velocity", velocity);
        stats.put("capacity", capacity);

        return stats;
    }

    @Override
    @Transactional
    public void addTaskToSprint(Long sprintId, Long taskId) {
        Sprint sprint = sprintMapper.findById(sprintId);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint not found");
        }
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        task.setSprintId(sprintId);

        // Auto-link task to sprint's milestone if sprint has one and task doesn't already have one
        if (sprint.getMilestoneId() != null && task.getMilestoneId() == null) {
            task.setMilestoneId(sprint.getMilestoneId());
        }

        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void removeTaskFromSprint(Long taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        task.setSprintId(null);
        taskMapper.updateById(task);
    }

    public int batchAddTasks(Long sprintId, List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        for (Long taskId : taskIds) {
            Task task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setSprintId(sprintId);
                taskMapper.updateById(task);
            }
        }
        return taskIds.size();
    }

    public int batchRemoveTasks(Long sprintId, List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        for (Long taskId : taskIds) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && sprintId.equals(task.getSprintId())) {
                task.setSprintId(null);
                taskMapper.updateById(task);
            }
        }
        return taskIds.size();
    }

    @Override
    public List<Task> getBacklogTasks(String projectId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId)
               .isNull(Task::getSprintId)
               .eq(Task::getDeleted, 0)
               .orderByAsc(Task::getCreatedAt);
        return taskMapper.selectList(wrapper);
    }
}
