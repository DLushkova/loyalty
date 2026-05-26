package com.clubloyalty.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GameSession extends BaseEntity {
    private int profileId;
    private int tariffId;
    private Integer promotionId;  //
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalCost;
    private BigDecimal bonusEarned;
    private BigDecimal bonusUsed;

    // геттеры и сеттеры
    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getTariffId() { return tariffId; }
    public void setTariffId(int tariffId) { this.tariffId = tariffId; }

    public Integer getPromotionId() { return promotionId; }  // ← ДОБАВИТЬ
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }  // ← ДОБАВИТЬ

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getBonusEarned() { return bonusEarned; }
    public void setBonusEarned(BigDecimal bonusEarned) { this.bonusEarned = bonusEarned; }

    public BigDecimal getBonusUsed() { return bonusUsed; }
    public void setBonusUsed(BigDecimal bonusUsed) { this.bonusUsed = bonusUsed; }

    @Override
    public String getDisplayInfo() {
        return "GameSession{id=" + getId() + ", profileId=" + profileId + ", startTime=" + startTime + "}";
    }
}