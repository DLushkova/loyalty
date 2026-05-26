package com.clubloyalty.server.dao;

import com.clubloyalty.server.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByLogin(String login) {
        String sql = "SELECT u.user_id, u.login, u.password_hash, u.role_id, u.is_active, u.is_blocked " +
                "FROM users u WHERE u.login = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRoleId(rs.getInt("role_id"));
                user.setActive(rs.getBoolean("is_active"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int userId) {
        String sql = "SELECT u.user_id, u.login, u.password_hash, u.role_id, u.is_active, u.is_blocked " +
                "FROM users u WHERE u.user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRoleId(rs.getInt("role_id"));
                user.setActive(rs.getBoolean("is_active"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.user_id, u.login, u.password_hash, u.role_id, u.is_active, u.is_blocked, " +
                "p.full_name FROM users u LEFT JOIN persons p ON u.user_id = p.user_id";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean create(User user) {
        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtPerson = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sqlUser = "INSERT INTO users (login, password_hash, role_id, is_active, is_blocked) VALUES (?, ?, ?, ?, ?)";
            stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getLogin());
            stmtUser.setString(2, user.getPasswordHash());
            stmtUser.setInt(3, user.getRoleId());
            stmtUser.setBoolean(4, user.isActive());
            stmtUser.setBoolean(5, user.isBlocked());
            stmtUser.executeUpdate();

            generatedKeys = stmtUser.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
                user.setId(userId);
            }

            String sqlPerson = "INSERT INTO persons (full_name, phone, email, user_id) VALUES (?, ?, ?, ?)";
            stmtPerson = conn.prepareStatement(sqlPerson);
            stmtPerson.setString(1, user.getFullName() != null ? user.getFullName() : user.getLogin());
            stmtPerson.setString(2, user.getPhone());
            stmtPerson.setString(3, user.getEmail());
            stmtPerson.setInt(4, userId);
            stmtPerson.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (stmtUser != null) stmtUser.close(); } catch (SQLException e) {}
            try { if (stmtPerson != null) stmtPerson.close(); } catch (SQLException e) {}
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
        return false;
    }

    public void updateBlocked(int userId, boolean blocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, blocked);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFullName(int userId, String fullName) {
        String sql = "UPDATE persons SET full_name = ? WHERE user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int userId) {
        String sqlPerson = "DELETE FROM persons WHERE user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sqlPerson)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlUser = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sqlUser)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setLogin(rs.getString("login"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRoleId(rs.getInt("role_id"));
        user.setActive(rs.getBoolean("is_active"));
        user.setBlocked(rs.getBoolean("is_blocked"));
        try {
            user.setFullName(rs.getString("full_name"));
        } catch (SQLException e) {
            user.setFullName(user.getLogin());
        }
        return user;
    }
}