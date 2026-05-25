package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TaskStatusChangedEvent extends BaseNotificationEvent {
    private final Long taskId;
    private final String oldStatusCode;
    private final String newStatusCode;

    public TaskStatusChangedEvent(Object source, Long userId, Long taskId, String oldStatusCode,
                                  String newStatusCode, String title, String content, String relatedProjectId) {
        super(source, userId, title, content, taskId, relatedProjectId);
        this.taskId = taskId;
        this.oldStatusCode = oldStatusCode;
        this.newStatusCode = newStatusCode;
    }
}
