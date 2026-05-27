package com.sme.pm.service.impl;

import com.sme.pm.entity.Milestone;
import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.MilestoneMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.IProjectStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectStatsServiceImpl implements IProjectStatsService {

    private final ProjectMapper projectMapper;
    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final MilestoneMapper milestoneMapper;

    public ProjectStatsServiceImpl(ProjectMapper projectMapper, SprintMapper sprintMapper,
                                   TaskMapper taskMapper, MilestoneMapper milestoneMapper) {
        this.projectMapper = projectMapper;
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
        this.milestoneMapper = milestoneMapper;
    }

    @Override
    public Map<String, Object> getProjectStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> kpi = new HashMap<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return stats;
        }

        List<Task> tasks = taskMapper.findByProjectId(project.getProjectId());
        LocalDate today = LocalDate.now();

        int totalTasks = tasks.size();
        int completedTasks = 0;
        int inProgressTasks = 0;
        int blockedTasks = 0;
        int overdueTasks = 0;
        int totalEstimateHours = 0;
        int completedHours = 0;

        for (Task task : tasks) {
            totalEstimateHours += (task.getEstimateHours() != null) ? task.getEstimateHours() : 0;
            if (task.getProgress() != null) {
                if (task.getProgress() == 100) {
                    completedTasks++;
                    completedHours += (task.getEstimateHours() != null) ? task.getEstimateHours() : 0;
                } else if (task.getProgress() > 0) {
                    inProgressTasks++;
                    // Check if task is overdue
                    if (task.getDueDate() != null && task.getDueDate().isBefore(today)) {
                        overdueTasks++;
                    }
                }
            }
            // Check if task is blocked (status indicates blocked)
            if ("BLOCKED".equals(task.getStatus())) {
                blockedTasks++;
            }
        }

        // Completion rate
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        // Work efficiency (completed hours vs total estimated)
        double workEfficiency = totalEstimateHours > 0 ? (double) completedHours / totalEstimateHours * 100 : 0;

        // Defect density (placeholder - would need defect tracking)
        double defectDensity = 0;

        // Milestone achievement (calculate from actual milestones)
        double milestoneAchievement = calculateMilestoneAchievement(project.getProjectId());

        kpi.put("totalTasks", totalTasks);
        kpi.put("completed", completedTasks);
        kpi.put("inProgress", inProgressTasks);
        kpi.put("blocked", blockedTasks);
        kpi.put("overdue", overdueTasks);

        stats.put("kpi", kpi);
        stats.put("projectId", projectId);
        stats.put("projectName", project.getName());
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("blockedTasks", blockedTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", Math.round(completionRate * 100) / 100.0);
        stats.put("workEfficiency", Math.round(workEfficiency * 100) / 100.0);
        stats.put("defectDensity", defectDensity);
        stats.put("milestoneAchievement", Math.round(milestoneAchievement * 100) / 100.0);
        stats.put("totalEstimateHours", totalEstimateHours);
        stats.put("completedHours", completedHours);

        return stats;
    }

    private double calculateMilestoneAchievement(String projectId) {
        List<Milestone> milestones = milestoneMapper.findByProjectId(projectId);
        if (milestones == null || milestones.isEmpty()) {
            return 0;
        }
        long completedCount = milestones.stream()
                .filter(m -> "COMPLETED".equals(m.getStatus()))
                .count();
        return (double) completedCount / milestones.size() * 100;
    }

    @Override
    public List<Map<String, Object>> compareProjects(List<Long> projectIds) {
        List<Map<String, Object>> comparisons = new ArrayList<>();

        for (Long projectId : projectIds) {
            Map<String, Object> projectStats = getProjectStats(projectId);
            comparisons.add(projectStats);
        }

        return comparisons;
    }

    @Override
    public Map<String, Object> getTeamThroughput(Long projectId, String startDate, String endDate) {
        Map<String, Object> throughput = new HashMap<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return throughput;
        }

        List<Task> tasks = taskMapper.findByProjectId(project.getProjectId());

        // Filter tasks by date range if provided
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        List<Task> filteredTasks = tasks.stream()
                .filter(task -> {
                    if (start == null || end == null) {
                        return true;
                    }
                    LocalDateTime createdAt = task.getCreatedAt();
                    if (createdAt == null) {
                        return false;
                    }
                    LocalDate taskDate = createdAt.toLocalDate();
                    return !taskDate.isBefore(start) && !taskDate.isAfter(end);
                })
                .filter(task -> task.getCompletionDate() != null)
                .collect(Collectors.toList());

        // Group by week
        Map<String, Integer> weeklyThroughput = new LinkedHashMap<>();
        Map<String, Integer> dailyThroughput = new LinkedHashMap<>();

        for (Task task : filteredTasks) {
            LocalDate completionDate = task.getCompletionDate().toLocalDate();

            // Weekly grouping
            LocalDate weekStart = completionDate.minusDays(completionDate.getDayOfWeek().getValue() - 1);
            String weekKey = weekStart.toString();
            weeklyThroughput.merge(weekKey, 1, Integer::sum);

            // Daily grouping
            String dailyKey = completionDate.toString();
            dailyThroughput.merge(dailyKey, 1, Integer::sum);
        }

        int totalCompleted = filteredTasks.size();

        throughput.put("totalCompleted", totalCompleted);
        throughput.put("weeklyThroughput", weeklyThroughput);
        throughput.put("dailyThroughput", dailyThroughput);
        throughput.put("startDate", startDate);
        throughput.put("endDate", endDate);

        return throughput;
    }

    @Override
    public Map<String, Object> getBurndownData(Long sprintId) {
        Map<String, Object> burndown = new HashMap<>();

        Sprint sprint = sprintMapper.findById(sprintId);
        if (sprint == null) {
            return burndown;
        }

        List<Task> tasks = taskMapper.findBySprintId(sprintId);
        int totalTasks = tasks.size();
        int totalEstimateHours = 0;

        for (Task task : tasks) {
            totalEstimateHours += (task.getEstimateHours() != null) ? task.getEstimateHours() : 0;
        }

        // Calculate sprint duration in days
        LocalDate startDate = sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate();

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(14); // Default 2 weeks
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) {
            totalDays = 14;
        }

        // Calculate ideal burndown (linear)
        List<Map<String, Object>> idealData = new ArrayList<>();
        int dailyBurnRate = totalEstimateHours / (int) totalDays;

        for (int i = 0; i <= totalDays; i++) {
            Map<String, Object> point = new HashMap<>();
            LocalDate date = startDate.plusDays(i);
            point.put("date", date.toString());
            point.put("remainingHours", totalEstimateHours - (i * dailyBurnRate));
            idealData.add(point);
        }

        // Calculate actual burndown based on task completion
        List<Map<String, Object>> actualData = new ArrayList<>();
        Map<LocalDate, Integer> dailyCompleted = new HashMap<>();

        for (Task task : tasks) {
            if (task.getCompletionDate() != null && task.getEstimateHours() != null) {
                LocalDate completionDate = task.getCompletionDate().toLocalDate();
                dailyCompleted.merge(completionDate, task.getEstimateHours(), Integer::sum);
            }
        }

        int cumulativeCompleted = 0;
        for (int i = 0; i <= totalDays; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());

            // Add completed hours for this day
            cumulativeCompleted += dailyCompleted.getOrDefault(date, 0);
            point.put("remainingHours", totalEstimateHours - cumulativeCompleted);
            point.put("completedHours", cumulativeCompleted);

            actualData.add(point);
        }

        burndown.put("sprintId", sprintId);
        burndown.put("sprintName", sprint.getName());
        burndown.put("totalHours", totalEstimateHours);
        burndown.put("totalTasks", totalTasks);
        burndown.put("totalDays", totalDays);
        burndown.put("idealBurndown", idealData);
        burndown.put("actualBurndown", actualData);

        return burndown;
    }

    @Override
    public List<Map<String, Object>> getCfdData(Long projectId) {
        List<Map<String, Object>> cfdData = new ArrayList<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return cfdData;
        }

        List<Task> tasks = taskMapper.findByProjectId(project.getProjectId());
        if (tasks.isEmpty()) {
            return cfdData;
        }

        // Find date range from tasks
        LocalDate minDate = LocalDate.now().minusDays(30);
        LocalDate maxDate = LocalDate.now();

        for (Task task : tasks) {
            if (task.getCreatedAt() != null) {
                LocalDate createdDate = task.getCreatedAt().toLocalDate();
                if (createdDate.isBefore(minDate)) {
                    minDate = createdDate;
                }
            }
        }

        // Generate daily CFD data
        LocalDate currentDate = minDate;
        while (!currentDate.isAfter(maxDate)) {
            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("date", currentDate.toString());

            final LocalDate checkDate = currentDate;
            int todoCount = 0;
            int inProgressCount = 0;
            int doneCount = 0;

            for (Task task : tasks) {
                LocalDateTime createdAt = task.getCreatedAt();
                if (createdAt == null) continue;

                LocalDate createdDate = createdAt.toLocalDate();
                if (createdDate.isAfter(checkDate)) continue;

                LocalDateTime completionDate = task.getCompletionDate();
                if (completionDate != null && completionDate.toLocalDate().isBefore(checkDate)) {
                    doneCount++;
                } else if ("DONE".equals(task.getStatus())) {
                    doneCount++;
                } else if ("IN_PROGRESS".equals(task.getStatus()) || "IN_REVIEW".equals(task.getStatus())) {
                    inProgressCount++;
                } else {
                    todoCount++;
                }
            }

            dayData.put("todo", todoCount);
            dayData.put("in_progress", inProgressCount);
            dayData.put("done", doneCount);
            cfdData.add(dayData);

            currentDate = currentDate.plusDays(1);
        }

        return cfdData;
    }

    @Override
    public List<Map<String, Object>> getHeatmapData(Long projectId) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return heatmapData;
        }

        List<Task> tasks = taskMapper.findByProjectId(project.getProjectId());

        // Group by assignee and priority
        Map<String, Map<String, Long>> heatmapMap = new HashMap<>();

        for (Task task : tasks) {
            String assignee = task.getAssigneeName();
            String priority = task.getPriority();

            if (assignee == null) assignee = "Unassigned";
            if (priority == null) priority = "None";

            heatmapMap.computeIfAbsent(assignee, k -> new HashMap<>())
                    .merge(priority, 1L, Long::sum);
        }

        // Convert to list format
        for (Map.Entry<String, Map<String, Long>> assigneeEntry : heatmapMap.entrySet()) {
            for (Map.Entry<String, Long> priorityEntry : assigneeEntry.getValue().entrySet()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("assignee", assigneeEntry.getKey());
                entry.put("priority", priorityEntry.getKey());
                entry.put("count", priorityEntry.getValue());
                heatmapData.add(entry);
            }
        }

        return heatmapData;
    }

    @Override
    public List<Map<String, Object>> getMilestoneProgress(Long projectId) {
        List<Map<String, Object>> milestoneData = new ArrayList<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return milestoneData;
        }

        List<Milestone> milestones = milestoneMapper.findByProjectId(project.getProjectId());
        List<Task> allTasks = taskMapper.findByProjectId(project.getProjectId());

        for (Milestone milestone : milestones) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", milestone.getId());
            data.put("name", milestone.getName());
            data.put("status", milestone.getStatus());
            data.put("targetDate", milestone.getTargetDate());

            // Count tasks for this milestone
            int total = 0;
            int completed = 0;

            for (Task task : allTasks) {
                if (milestone.getId().equals(task.getMilestoneId())) {
                    total++;
                    if ("DONE".equals(task.getStatus()) || task.getProgress() == 100) {
                        completed++;
                    }
                }
            }

            data.put("total", total);
            data.put("completed", completed);
            data.put("percent", total > 0 ? Math.round((double) completed / total * 100) : 0);

            milestoneData.add(data);
        }

        return milestoneData;
    }
}
