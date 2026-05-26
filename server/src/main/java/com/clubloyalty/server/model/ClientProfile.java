package com.clubloyalty.server.model;

import java.math.BigDecimal;

public class ClientProfile extends BaseEntity {
    private int userId;
    private BigDecimal bonusBalance = BigDecimal.ZERO;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal moneyBalance = BigDecimal.ZERO;
    private int statusId;

    // геттеры и сеттеры
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public BigDecimal getBonusBalance() { return bonusBalance; }
    public void setBonusBalance(BigDecimal bonusBalance) { this.bonusBalance = bonusBalance; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public BigDecimal getMoneyBalance() { return moneyBalance; }
    public void setMoneyBalance(BigDecimal moneyBalance) { this.moneyBalance = moneyBalance; }
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    @Override
    public String getDisplayInfo() {
        return "ClientProfile{userId=" + userId + ", bonus=" + bonusBalance + ", money=" + moneyBalance + "}";
    }
}