package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TaskStatusChangedEvent extends BaseNotificationEvent {
    private final Long taskId;
    private final Long oldStatusId;
    private final Long newStatusId;

    public TaskStatusChangedEvent(Object source, Long userId, Long taskId, Long oldStatusId,
                                  Long newStatusId, String title, String content, Long relatedProjectId) {
        super(source, userId, title, content, taskId, relatedProjectId);
        this.taskId = taskId;
        this.oldStatusId = oldStatusId;
        this.newStatusId = newStatusId;
    }
}
