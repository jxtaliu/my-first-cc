package com.sme.pm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public abstract class BaseNotificationEvent extends ApplicationEvent {
    private final Long userId;
    private final String title;
    private final String content;
    private final Long relatedTaskId;
    private final Long relatedProjectId;
    private final LocalDateTime eventTime;

    protected BaseNotificationEvent(Object source, Long userId, String title, String content,
                                    Long relatedTaskId, Long relatedProjectId) {
        super(source);
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.relatedTaskId = relatedTaskId;
        this.relatedProjectId = relatedProjectId;
        this.eventTime = LocalDateTime.now();
    }
}
