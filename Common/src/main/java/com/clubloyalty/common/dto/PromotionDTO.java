package com.clubloyalty.common.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class PromotionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int promotionId;
    private String name;
    private double bonusMultiplier;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;

    public PromotionDTO() {}

    public int getPromotionId() { return promotionId; }
    public void setPromotionId(int promotionId) { this.promotionId = promotionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBonusMultiplier() { return bonusMultiplier; }
    public void setBonusMultiplier(double bonusMultiplier) { this.bonusMultiplier = bonusMultiplier; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}