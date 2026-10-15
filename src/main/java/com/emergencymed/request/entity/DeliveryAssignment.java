package com.emergencymed.request.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_assignments", indexes = {
        @Index(name = "idx_das_partner_status", columnList = "partner_user_id, status")
})
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "partner_user_id", nullable = false)
    private Long partnerUserId;

    @Column(nullable = false, length = 10)
    private String status; // OFFERED, ACCEPTED, REJECTED, EXPIRED

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "offered_at", nullable = false)
    private LocalDateTime offeredAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    public DeliveryAssignment() {
    }

    public DeliveryAssignment(Long requestId, Long partnerUserId, String status, int attemptNo, LocalDateTime offeredAt, LocalDateTime expiresAt) {
        this.requestId = requestId;
        this.partnerUserId = partnerUserId;
        this.status = status;
        this.attemptNo = attemptNo;
        this.offeredAt = offeredAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getPartnerUserId() {
        return partnerUserId;
    }

    public void setPartnerUserId(Long partnerUserId) {
        this.partnerUserId = partnerUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(int attemptNo) {
        this.attemptNo = attemptNo;
    }

    public LocalDateTime getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(LocalDateTime offeredAt) {
        this.offeredAt = offeredAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
