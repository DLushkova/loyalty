package com.clubloyalty.server.dao;

import com.clubloyalty.server.model.Promotion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {

    public List<Promotion> findAll() {
        List<Promotion> promotions = new ArrayList<>();
        String sql = "SELECT * FROM promotion";  // ← promotion, а не promotions
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Promotion promotion = new Promotion();
                promotion.setId(rs.getInt("promotion_id"));
                promotion.setName(rs.getString("promotion_name"));
                promotion.setBonusMultiplier(rs.getDouble("bonus_multiplier"));
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                if (startDate != null) promotion.setStartDate(startDate.toLocalDate());
                if (endDate != null) promotion.setEndDate(endDate.toLocalDate());
                promotion.setActive(rs.getBoolean("is_active"));
                promotions.add(promotion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promotions;
    }

    public void create(Promotion promotion) {
        String sql = "INSERT INTO promotion (promotion_name, bonus_multiplier, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, promotion.getName());
            stmt.setDouble(2, promotion.getBonusMultiplier());
            stmt.setDate(3, Date.valueOf(promotion.getStartDate()));
            stmt.setDate(4, Date.valueOf(promotion.getEndDate()));
            stmt.setBoolean(5, promotion.isActive());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                promotion.setId(rs.getInt(1));
            }
            System.out.println("Акция добавлена: " + promotion.getName());
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении акции: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(int promotionId, String name, double multiplier) {
        String sql = "UPDATE promotion SET promotion_name = ?, bonus_multiplier = ? WHERE promotion_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, multiplier);
            stmt.setInt(3, promotionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM promotion WHERE promotion_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}