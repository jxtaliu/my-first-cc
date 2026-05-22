package com.sme.pm.event;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MilestoneDueEvent extends BaseNotificationEvent {
    private final Long milestoneId;
    private final LocalDate targetDate;

    public MilestoneDueEvent(Object source, Long userId, Long milestoneId, LocalDate targetDate,
                              String title, String content, String relatedProjectId) {
        super(source, userId, title, content, null, relatedProjectId);
        this.milestoneId = milestoneId;
        this.targetDate = targetDate;
    }
}
