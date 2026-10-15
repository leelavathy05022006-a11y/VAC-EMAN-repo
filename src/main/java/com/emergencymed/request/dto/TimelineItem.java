package com.emergencymed.request.dto;

import java.time.LocalDateTime;

public class TimelineItem {
    private String fromStatus;
    private String toStatus;
    private Long changedByUserId;
    private String changedByName;
    private String note;
    private LocalDateTime timestamp;

    public TimelineItem() {
    }

    public TimelineItem(String fromStatus, String toStatus, Long changedByUserId, String changedByName, String note, LocalDateTime timestamp) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.changedByName = changedByName;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
