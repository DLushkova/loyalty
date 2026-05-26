package com.clubloyalty.common.dto;

import java.io.Serializable;

public class TariffDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int tariffId;
    private String name;
    private double pricePerHour;
    private double bonusPerHour;
    private boolean isActive;
    private String description;

    public TariffDTO() {}

    public int getTariffId() { return tariffId; }
    public void setTariffId(int tariffId) { this.tariffId = tariffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public double getBonusPerHour() { return bonusPerHour; }
    public void setBonusPerHour(double bonusPerHour) { this.bonusPerHour = bonusPerHour; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " (" + pricePerHour + " руб/час, +" + bonusPerHour + " бонусов/час)";
    }
}