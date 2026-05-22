package com.sme.pm.event;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TaskCommentEvent extends BaseNotificationEvent {
    private final Long taskId;
    private final Long commentId;
    private final String[] mentions;

    public TaskCommentEvent(Object source, Long userId, Long taskId, Long commentId,
                             String[] mentions, String title, String content, Long relatedProjectId) {
        super(source, userId, title, content, taskId, relatedProjectId);
        this.taskId = taskId;
        this.commentId = commentId;
        this.mentions = mentions != null ? mentions : new String[0];
    }
}
