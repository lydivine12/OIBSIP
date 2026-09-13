package com.library.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fines")
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "issue_record_id")
    private IssueRecord issueRecord;

    private BigDecimal amount;

    private boolean paid = false;

    public Fine() {
    }

    public Fine(IssueRecord issueRecord, BigDecimal amount) {
        this.issueRecord = issueRecord;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IssueRecord getIssueRecord() {
        return issueRecord;
    }

    public void setIssueRecord(IssueRecord issueRecord) {
        this.issueRecord = issueRecord;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
