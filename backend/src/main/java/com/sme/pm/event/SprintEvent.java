package com.sme.pm.event;

import lombok.Getter;

@Getter
public class SprintEvent extends BaseNotificationEvent {
    private final Long sprintId;
    private final String eventType;

    public SprintEvent(Object source, Long userId, Long sprintId, String eventType,
                       String title, String content, String relatedProjectId) {
        super(source, userId, title, content, null, relatedProjectId);
        this.sprintId = sprintId;
        this.eventType = eventType;
    }
}
