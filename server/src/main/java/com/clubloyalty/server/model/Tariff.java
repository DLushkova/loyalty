package com.clubloyalty.server.model;

import java.math.BigDecimal;

public class Tariff extends BaseEntity {
    private String name;
    private BigDecimal pricePerHour;
    private boolean isActive;
    private String description;

    @Override
    public String getDisplayInfo() {
        return "Тариф: " + name + " - " + pricePerHour + " руб/час";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    private double bonusPerHour;
    public double getBonusPerHour() { return bonusPerHour; }
    public void setBonusPerHour(double bonusPerHour) { this.bonusPerHour = bonusPerHour; }
}