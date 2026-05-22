package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TaskDependencyBlockedEvent extends BaseNotificationEvent {
    private final Long taskId;

    public TaskDependencyBlockedEvent(Object source, Long userId, String title, String content,
                                      Long taskId, Long relatedProjectId) {
        super(source, userId, title, content, taskId, relatedProjectId);
        this.taskId = taskId;
    }
}
