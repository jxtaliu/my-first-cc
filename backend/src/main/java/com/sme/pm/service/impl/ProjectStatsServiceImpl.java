package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
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

    public ProjectStatsServiceImpl(ProjectMapper projectMapper, SprintMapper sprintMapper, TaskMapper taskMapper) {
        this.projectMapper = projectMapper;
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public Map<String, Object> getProjectStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        Project project = projectMapper.findById(projectId);
        if (project == null) {
            return stats;
        }

        List<Task> tasks = taskMapper.findByProjectId(project.getProjectId());
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int inProgressTasks = 0;
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
                }
            }
        }

        // Completion rate
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        // Work efficiency (completed hours vs total estimated)
        double workEfficiency = totalEstimateHours > 0 ? (double) completedHours / totalEstimateHours * 100 : 0;

        // Defect density (placeholder - would need defect tracking)
        double defectDensity = 0;

        // Milestone achievement (placeholder - would need milestone data)
        double milestoneAchievement = 0;

        stats.put("projectId", projectId);
        stats.put("projectName", project.getName());
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("completionRate", Math.round(completionRate * 100) / 100.0);
        stats.put("workEfficiency", Math.round(workEfficiency * 100) / 100.0);
        stats.put("defectDensity", defectDensity);
        stats.put("milestoneAchievement", milestoneAchievement);
        stats.put("totalEstimateHours", totalEstimateHours);
        stats.put("completedHours", completedHours);

        return stats;
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
        LocalDateTime startDate = sprint.getStartDate();
        LocalDateTime endDate = sprint.getEndDate();

        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(14); // Default 2 weeks
        }

        long totalDays = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        if (totalDays <= 0) {
            totalDays = 14;
        }

        // Calculate ideal burndown (linear)
        List<Map<String, Object>> idealData = new ArrayList<>();
        int dailyBurnRate = totalEstimateHours / (int) totalDays;

        for (int i = 0; i <= totalDays; i++) {
            Map<String, Object> point = new HashMap<>();
            LocalDate date = startDate.toLocalDate().plusDays(i);
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
            LocalDate date = startDate.toLocalDate().plusDays(i);
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
}
