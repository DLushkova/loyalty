package com.clubloyalty.server.dao;

import com.clubloyalty.server.model.ClientProfile;
import java.math.BigDecimal;
import java.sql.*;

public class ClientProfileDAO {

    public ClientProfile findByUserId(int userId) {
        String sql = "SELECT * FROM clientprofiles WHERE user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProfile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClientProfile findById(int profileId) {
        String sql = "SELECT * FROM clientprofiles WHERE profile_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProfile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void create(ClientProfile profile) {
        String sql = "INSERT INTO clientprofiles (user_id, bonus_balance, total_spent, money_balance, status_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, profile.getUserId());
            stmt.setBigDecimal(2, profile.getBonusBalance());
            stmt.setBigDecimal(3, profile.getTotalSpent());
            stmt.setBigDecimal(4, profile.getMoneyBalance());
            stmt.setInt(5, profile.getStatusId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                profile.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBonusBalance(int profileId, BigDecimal newBalance) {
        String sql = "UPDATE clientprofiles SET bonus_balance = ? WHERE profile_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTotalSpent(int profileId, BigDecimal amount) {
        String sql = "UPDATE clientprofiles SET total_spent = total_spent + ? WHERE profile_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(int profileId, int statusId) {
        String sql = "UPDATE clientprofiles SET status_id = ? WHERE profile_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, statusId);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMoneyBalance(int profileId, BigDecimal newBalance) {
        String sql = "UPDATE clientprofiles SET money_balance = ? WHERE profile_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ClientProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        ClientProfile profile = new ClientProfile();
        profile.setId(rs.getInt("profile_id"));
        profile.setUserId(rs.getInt("user_id"));
        profile.setBonusBalance(rs.getBigDecimal("bonus_balance"));
        profile.setTotalSpent(rs.getBigDecimal("total_spent"));
        profile.setMoneyBalance(rs.getBigDecimal("money_balance"));
        profile.setStatusId(rs.getInt("status_id"));
        return profile;
    }
}