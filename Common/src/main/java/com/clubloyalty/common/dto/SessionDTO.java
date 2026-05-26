package com.clubloyalty.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int sessionId;
    private String tariffName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double totalCost;
    private double bonusEarned;
    private double bonusUsed;

    // геттеры и сеттеры
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getTariffName() { return tariffName; }
    public void setTariffName(String tariffName) { this.tariffName = tariffName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getBonusEarned() { return bonusEarned; }
    public void setBonusEarned(double bonusEarned) { this.bonusEarned = bonusEarned; }

    public double getBonusUsed() { return bonusUsed; }
    public void setBonusUsed(double bonusUsed) { this.bonusUsed = bonusUsed; }
}