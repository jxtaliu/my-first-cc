package com.sme.pm.event;

import lombok.Getter;

@Getter
public class TimesheetApprovalEvent extends BaseNotificationEvent {
    private final Long timesheetId;
    private final String approvalStatus;

    public TimesheetApprovalEvent(Object source, Long userId, Long timesheetId, String approvalStatus,
                                   String title, String content, Long relatedProjectId) {
        super(source, userId, title, content, null, relatedProjectId);
        this.timesheetId = timesheetId;
        this.approvalStatus = approvalStatus;
    }
}
