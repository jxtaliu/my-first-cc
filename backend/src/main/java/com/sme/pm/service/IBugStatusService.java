package com.sme.pm.service;

import com.sme.pm.entity.BugStatus;

import java.util.List;

public interface IBugStatusService {

    /**
     * Get bug statuses for a project
     */
    List<BugStatus> getByProjectId(String projectId);

    /**
     * Get default bug statuses (system-wide)
     */
    List<BugStatus> getDefaultStatuses();

    /**
     * Check if a status transition is valid
     */
    boolean canTransition(String projectId, String fromStatus, String toStatus);

    /**
     * Get allowed target statuses for a given status
     */
    List<String> getAllowedTransitions(String projectId, String fromStatus);

    /**
     * Initialize bug statuses for a new project
     */
    void initializeForProject(String projectId);
}
