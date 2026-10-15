package com.emergencymed.request.dto;

public class RequestSummaryResponse {
    private long pending;
    private long inProgress;
    private long completed;
    private long cancelled;
    private long total;

    public RequestSummaryResponse() {
    }

    public RequestSummaryResponse(long pending, long inProgress, long completed, long cancelled, long total) {
        this.pending = pending;
        this.inProgress = inProgress;
        this.completed = completed;
        this.cancelled = cancelled;
        this.total = total;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getCancelled() {
        return cancelled;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
