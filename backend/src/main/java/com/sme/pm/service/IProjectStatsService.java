package com.sme.pm.service;

import java.util.List;
import java.util.Map;

public interface IProjectStatsService {
    Map<String, Object> getProjectStats(String projectId);
    List<Map<String, Object>> compareProjects(List<String> projectIds);
    Map<String, Object> getTeamThroughput(String projectId, String startDate, String endDate);
    Map<String, Object> getBurndownData(Long sprintId);
    List<Map<String, Object>> getCfdData(String projectId);
    List<Map<String, Object>> getHeatmapData(String projectId);
    List<Map<String, Object>> getMilestoneProgress(String projectId);
}
