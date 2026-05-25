package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BugStatusTest {

    @Test
    void bugStatus_shouldStoreAllFields() {
        BugStatus status = new BugStatus();
        status.setId(1L);
        status.setProjectId("PRJ_001");
        status.setCode("OPEN");
        status.setNameEn("Open");
        status.setNameZh("待办");
        status.setColor("#EF4444");
        status.setSortOrder(1);
        status.setDeleted(0);

        assertEquals(1L, status.getId());
        assertEquals("PRJ_001", status.getProjectId());
        assertEquals("OPEN", status.getCode());
        assertEquals("Open", status.getNameEn());
        assertEquals("待办", status.getNameZh());
        assertEquals("#EF4444", status.getColor());
        assertEquals(1, status.getSortOrder());
        assertEquals(0, status.getDeleted());
    }

    @Test
    void bugStatusTransition_shouldStoreAllFields() {
        BugStatusTransition transition = new BugStatusTransition();
        transition.setId(1L);
        transition.setProjectId("PRJ_001");
        transition.setFromStatus("OPEN");
        transition.setToStatus("IN_PROGRESS");
        transition.setDeleted(0);

        assertEquals(1L, transition.getId());
        assertEquals("PRJ_001", transition.getProjectId());
        assertEquals("OPEN", transition.getFromStatus());
        assertEquals("IN_PROGRESS", transition.getToStatus());
        assertEquals(0, transition.getDeleted());
    }

    @Test
    void bugStatus_shouldAllowNullProjectId_forSystemDefaults() {
        BugStatus status = new BugStatus();
        status.setCode("OPEN");
        status.setNameEn("Open");

        assertNull(status.getProjectId());
    }
}
