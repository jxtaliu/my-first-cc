package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TaskAssignedEvent extends BaseNotificationEvent {
    private final Long taskId;
    private final Long assigneeId;

    public TaskAssignedEvent(Object source, Long userId, Long taskId, Long assigneeId,
                             String title, String content, Long relatedProjectId) {
        super(source, userId, title, content, taskId, relatedProjectId);
        this.taskId = taskId;
        this.assigneeId = assigneeId;
    }
}
