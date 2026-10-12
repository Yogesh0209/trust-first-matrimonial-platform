package com.tfmsp.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "price_per_month", nullable = false)
    private BigDecimal pricePerMonth = BigDecimal.ZERO;

    @Column(name = "member_limit")
    private Integer memberLimit;

    @Column(name = "gov_id_verification", nullable = false)
    private boolean govIdVerification = false;

    @Column(nullable = false)
    private String status = "ACTIVE";

    public Plan() {
    }

    public Plan(String name, BigDecimal pricePerMonth, Integer memberLimit, boolean govIdVerification) {
        this.name = name;
        this.pricePerMonth = pricePerMonth;
        this.memberLimit = memberLimit;
        this.govIdVerification = govIdVerification;
        this.status = "ACTIVE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(BigDecimal pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public boolean isGovIdVerification() {
        return govIdVerification;
    }

    public void setGovIdVerification(boolean govIdVerification) {
        this.govIdVerification = govIdVerification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
