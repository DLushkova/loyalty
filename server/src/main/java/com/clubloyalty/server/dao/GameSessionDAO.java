package com.clubloyalty.server.dao;

import com.clubloyalty.server.model.GameSession;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameSessionDAO {

    public void create(GameSession session) {
        String sql = "INSERT INTO gamesessions (profile_id, tariff_id, promotion_id, start_time, end_time, total_cost, bonus_earned, bonus_used) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getProfileId());
            stmt.setInt(2, session.getTariffId());
            if (session.getPromotionId() != null) {
                stmt.setInt(3, session.getPromotionId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(session.getStartTime()));

            // ВАЖНО: end_time может быть NULL для активной сессии
            if (session.getEndTime() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(session.getEndTime()));
            } else {
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }

            stmt.setBigDecimal(6, session.getTotalCost());
            stmt.setBigDecimal(7, session.getBonusEarned());
            stmt.setBigDecimal(8, session.getBonusUsed());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                session.setId(rs.getInt(1));
            }
            System.out.println("create: sessionId=" + session.getId() + ", endTime=" + session.getEndTime());
        } catch (SQLException e) {
            System.out.println("SQL ошибка в create: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(GameSession session) {
        String sql = "UPDATE gamesessions SET end_time = ?, total_cost = ?, bonus_earned = ?, bonus_used = ? WHERE session_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(session.getEndTime()));
            stmt.setBigDecimal(2, session.getTotalCost());
            stmt.setBigDecimal(3, session.getBonusEarned());
            stmt.setBigDecimal(4, session.getBonusUsed());
            stmt.setInt(5, session.getId());
            int affected = stmt.executeUpdate();
            System.out.println("update: affected rows = " + affected);
            if (affected == 0) {
                System.out.println("ОШИБКА: Сессия с ID=" + session.getId() + " не найдена!");
            }
        } catch (SQLException e) {
            System.out.println("SQL ошибка в update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public GameSession findById(int sessionId) {
        String sql = "SELECT * FROM gamesessions WHERE session_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GameSession session = new GameSession();
                session.setId(rs.getInt("session_id"));
                session.setProfileId(rs.getInt("profile_id"));
                session.setTariffId(rs.getInt("tariff_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                Timestamp endTime = rs.getTimestamp("end_time");
                if (endTime != null) {
                    session.setEndTime(endTime.toLocalDateTime());
                }
                session.setTotalCost(rs.getBigDecimal("total_cost"));
                session.setBonusEarned(rs.getBigDecimal("bonus_earned"));
                session.setBonusUsed(rs.getBigDecimal("bonus_used"));
                System.out.println("findById: sessionId=" + sessionId + ", endTime=" + session.getEndTime());
                return session;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GameSession findActiveByProfileId(int profileId) {
        String sql = "SELECT * FROM gamesessions WHERE profile_id = ? AND end_time IS NULL";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("findActiveByProfileId: найдена активная сессия");
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("findActiveByProfileId: активных сессий нет");
        return null;
    }

    public List<GameSession> findByProfileId(int profileId) {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM gamesessions WHERE profile_id = ? ORDER BY start_time DESC LIMIT 50";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("findByProfileId: profileId=" + profileId + ", найдено=" + sessions.size());
        return sessions;
    }

    public List<GameSession> findByDateRange(LocalDate from, LocalDate to) {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM gamesessions WHERE DATE(start_time) BETWEEN ? AND ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    private GameSession mapResultSetToSession(ResultSet rs) throws SQLException {
        GameSession session = new GameSession();
        session.setId(rs.getInt("session_id"));
        session.setProfileId(rs.getInt("profile_id"));
        session.setTariffId(rs.getInt("tariff_id"));
        session.setPromotionId(rs.getInt("promotion_id"));
        session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        if (rs.getTimestamp("end_time") != null) {
            session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }
        session.setTotalCost(rs.getBigDecimal("total_cost"));
        session.setBonusEarned(rs.getBigDecimal("bonus_earned"));
        session.setBonusUsed(rs.getBigDecimal("bonus_used"));
        return session;
    }
}