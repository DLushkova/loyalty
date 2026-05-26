package com.clubloyalty.server.dao;

import com.clubloyalty.server.model.Tariff;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TariffDAO {

    public List<Tariff> findAll() {
        List<Tariff> tariffs = new ArrayList<>();
        String sql = "SELECT * FROM tariffs WHERE is_active = 1";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tariffs.add(mapResultSetToTariff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tariffs;
    }

    public Tariff findById(int id) {
        String sql = "SELECT * FROM tariffs WHERE tariff_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTariff(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void create(Tariff tariff) {
        String sql = "INSERT INTO tariffs (tariff_name, price_per_hour, is_active, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tariff.getName());
            stmt.setBigDecimal(2, tariff.getPricePerHour());
            stmt.setBoolean(3, tariff.isActive());
            stmt.setString(4, tariff.getDescription());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                tariff.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(int tariffId, String name, BigDecimal price, double bonusPerHour) {
        String sql = "UPDATE tariffs SET tariff_name = ?, price_per_hour = ?, bonus_per_hour = ? WHERE tariff_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setBigDecimal(2, price);
            stmt.setDouble(3, bonusPerHour);
            stmt.setInt(4, tariffId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM tariffs WHERE tariff_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Tariff mapResultSetToTariff(ResultSet rs) throws SQLException {
        Tariff tariff = new Tariff();
        tariff.setId(rs.getInt("tariff_id"));
        tariff.setName(rs.getString("tariff_name"));
        tariff.setPricePerHour(rs.getBigDecimal("price_per_hour"));
        tariff.setActive(rs.getBoolean("is_active"));
        tariff.setDescription(rs.getString("description"));
        tariff.setBonusPerHour(rs.getDouble("bonus_per_hour"));
        return tariff;
    }

}