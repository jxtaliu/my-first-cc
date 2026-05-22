package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TimesheetSubmittedEvent extends BaseNotificationEvent {
    private final Long timesheetId;

    public TimesheetSubmittedEvent(Object source, Long userId, Long timesheetId,
                                   String title, String content, String relatedProjectId) {
        super(source, userId, title, content, null, relatedProjectId);
        this.timesheetId = timesheetId;
    }
}
