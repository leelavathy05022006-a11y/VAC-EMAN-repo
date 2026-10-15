package com.emergencymed.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private PharmacyInventory inventory;

    @Column(name = "txn_type", nullable = false, length = 15)
    private String txnType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reserved_after", nullable = false)
    private Integer reservedAfter;

    @Column(name = "batch_no", nullable = false, length = 40)
    private String batchNo;

    @Column(name = "reference_code", length = 30)
    private String referenceCode;

    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(length = 255)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public InventoryTransaction() {}

    public InventoryTransaction(PharmacyInventory inventory, String txnType, Integer quantityChange, Integer quantityAfter, Integer reservedAfter, String batchNo, String referenceCode, Long performedByUserId, String remarks) {
        this.inventory = inventory;
        this.txnType = txnType;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.reservedAfter = reservedAfter;
        this.batchNo = batchNo;
        this.referenceCode = referenceCode;
        this.performedByUserId = performedByUserId;
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PharmacyInventory getInventory() {
        return inventory;
    }

    public void setInventory(PharmacyInventory inventory) {
        this.inventory = inventory;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public Integer getReservedAfter() {
        return reservedAfter;
    }

    public void setReservedAfter(Integer reservedAfter) {
        this.reservedAfter = reservedAfter;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Long getPerformedByUserId() {
        return performedByUserId;
    }

    public void setPerformedByUserId(Long performedByUserId) {
        this.performedByUserId = performedByUserId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
