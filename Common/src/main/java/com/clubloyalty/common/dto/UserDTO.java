package com.clubloyalty.common.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String login;
    private String fullName;
    private String phone;
    private String email;
    private int roleId;
    private double bonusBalance;
    private boolean isBlocked;
    private String loyaltyStatus;
    private double discountPercent;
    private double bonusMultiplier;

    public UserDTO() {}

    // userId
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // login
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    // fullName
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // phone
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // email
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // roleId
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    // bonusBalance
    public double getBonusBalance() { return bonusBalance; }
    public void setBonusBalance(double bonusBalance) { this.bonusBalance = bonusBalance; }

    // isBlocked
    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    // loyaltyStatus (ТОЛЬКО ОДИН РАЗ!)
    public String getLoyaltyStatus() { return loyaltyStatus; }
    public void setLoyaltyStatus(String loyaltyStatus) { this.loyaltyStatus = loyaltyStatus; }

    // discountPercent
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    // bonusMultiplier
    public double getBonusMultiplier() { return bonusMultiplier; }
    public void setBonusMultiplier(double bonusMultiplier) { this.bonusMultiplier = bonusMultiplier; }
}