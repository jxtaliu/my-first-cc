package com.sme.pm.service;

import java.util.List;
import java.util.Map;

public interface IProjectStatsService {
    Map<String, Object> getProjectStats(Long projectId);
    List<Map<String, Object>> compareProjects(List<Long> projectIds);
    Map<String, Object> getTeamThroughput(Long projectId, String startDate, String endDate);
    Map<String, Object> getBurndownData(Long sprintId);
    List<Map<String, Object>> getCfdData(Long projectId);
    List<Map<String, Object>> getHeatmapData(Long projectId);
    List<Map<String, Object>> getMilestoneProgress(Long projectId);
}
