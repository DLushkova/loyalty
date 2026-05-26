package com.clubloyalty.server.model;

import java.time.LocalDate;

public class Promotion extends BaseEntity {
    private String name;
    private double bonusMultiplier;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;

    // геттеры и сеттеры
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

    @Override
    public String getDisplayInfo() {
        return "Акция: " + name + " (x" + bonusMultiplier + ")";
    }
}