package com.clubloyalty.server.service;

import com.clubloyalty.common.dto.PromotionDTO;
import com.clubloyalty.common.dto.SessionDTO;
import com.clubloyalty.common.dto.TariffDTO;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import com.clubloyalty.server.dao.*;
import com.clubloyalty.server.interfaces.ServiceInterface;
import com.clubloyalty.server.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoyaltyService implements ServiceInterface {
    private final ClientProfileDAO profileDAO;
    private final GameSessionDAO sessionDAO;
    private final TariffDAO tariffDAO;
    private final UserDAO userDAO;
    private final PromotionDAO promotionDAO;

    public LoyaltyService() {
        this.profileDAO = new ClientProfileDAO();
        this.sessionDAO = new GameSessionDAO();
        this.tariffDAO = new TariffDAO();
        this.userDAO = new UserDAO();
        this.promotionDAO = new PromotionDAO();
    }

    @Override
    public Response execute(Request request) {
        return new Response("ERROR", "Используйте конкретные методы");
    }

    public Response getBonusBalance(int userId) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }
        return new Response("SUCCESS", "Баланс получен", profile.getBonusBalance());
    }

    public Response getMoneyBalance(int userId) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }
        return new Response("SUCCESS", "Баланс получен", profile.getMoneyBalance());
    }

    public Response getActiveSession(int userId) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }
        GameSession session = sessionDAO.findActiveByProfileId(profile.getId());

        System.out.println("getActiveSession: session=" + (session != null ? session.getId() : "null"));

        if (session == null) {
            return new Response("SUCCESS", "Нет активной сессии", null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());

        // ИСПРАВЛЕНО: всегда передаём endTime
        if (session.getEndTime() != null) {
            result.put("endTime", session.getEndTime().toString());
        } else {
            // Активная сессия — передаём время окончания (1 час от начала)
            result.put("endTime", session.getStartTime().plusHours(1).toString());
        }
        System.out.println("getActiveSession: endTime=" + result.get("endTime"));

        return new Response("SUCCESS", "Активная сессия", result);
    }

    public Response getMySessions(int userId) {
        System.out.println("=== getMySessions ===");
        System.out.println("userId=" + userId);

        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            System.out.println("Профиль не найден!");
            return new Response("ERROR", "Профиль не найден");
        }
        System.out.println("profileId=" + profile.getId());

        List<GameSession> sessions = sessionDAO.findByProfileId(profile.getId());
        System.out.println("Найдено сессий: " + sessions.size());

        List<SessionDTO> result = new ArrayList<>();
        for (GameSession s : sessions) {
            SessionDTO dto = new SessionDTO();
            dto.setSessionId(s.getId());
            dto.setStartTime(s.getStartTime());
            dto.setEndTime(s.getEndTime());
            dto.setTotalCost(s.getTotalCost() != null ? s.getTotalCost().doubleValue() : 0);
            dto.setBonusEarned(s.getBonusEarned() != null ? s.getBonusEarned().doubleValue() : 0);


            Tariff tariff = tariffDAO.findById(s.getTariffId());
            dto.setTariffName(tariff != null ? tariff.getName() : "Неизвестно");

            result.add(dto);
            System.out.println("Добавлена сессия: ID=" + s.getId() + ", start=" + s.getStartTime() + ", tariff=" + dto.getTariffName());
        }

        return new Response("SUCCESS", "Сессии получены", result);
    }

    public Response startSession(int userId, int tariffId, int hours, boolean payWithBonus) {
        System.out.println("=== startSession ===");
        System.out.println("userId=" + userId + ", tariffId=" + tariffId + ", hours=" + hours + ", payWithBonus=" + payWithBonus);

        User user = userDAO.findById(userId);
        if (user == null || user.isBlocked()) {
            return new Response("ERROR", "Пользователь не найден или заблокирован");
        }

        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }
        System.out.println("profileId=" + profile.getId());

        Tariff tariff = tariffDAO.findById(tariffId);
        if (tariff == null) {
            return new Response("ERROR", "Тариф не найден");
        }

        // Проверяем активную сессию
        GameSession activeSession = sessionDAO.findActiveByProfileId(profile.getId());
        if (activeSession != null) {
            System.out.println("Уже есть активная сессия: ID=" + activeSession.getId());
            return new Response("ERROR", "У вас уже есть активная сессия");
        }

        double totalCost = tariff.getPricePerHour().doubleValue() * hours;
        double bonusToEarn = tariff.getBonusPerHour() * hours;

        // Применяем акции
        Promotion activePromotion = getActivePromotion();
        if (activePromotion != null) {
            bonusToEarn = bonusToEarn * activePromotion.getBonusMultiplier();
            System.out.println("Применена акция: " + activePromotion.getName());
        }

        double bonusPayment = 0;
        double moneyPayment = totalCost;

        if (payWithBonus) {
            double maxBonusPayment = totalCost * 0.5;
            bonusPayment = Math.min(maxBonusPayment, profile.getBonusBalance().doubleValue());
            moneyPayment = totalCost - bonusPayment;
            System.out.println("Оплата бонусами: " + bonusPayment + ", деньгами: " + moneyPayment);
        }

        if (profile.getMoneyBalance().doubleValue() < moneyPayment) {
            return new Response("ERROR", "Недостаточно средств на балансе");
        }

        if (payWithBonus && bonusPayment > 0 && profile.getBonusBalance().doubleValue() < bonusPayment) {
            return new Response("ERROR", "Недостаточно бонусов");
        }

        // Списываем деньги и бонусы
        BigDecimal newMoneyBalance = profile.getMoneyBalance().subtract(BigDecimal.valueOf(moneyPayment));
        profileDAO.updateMoneyBalance(profile.getId(), newMoneyBalance);
        System.out.println("Списано денег: " + moneyPayment + ", новый баланс: " + newMoneyBalance);

        if (bonusPayment > 0) {
            BigDecimal newBonusBalance = profile.getBonusBalance().subtract(BigDecimal.valueOf(bonusPayment));
            profileDAO.updateBonusBalance(profile.getId(), newBonusBalance);
            System.out.println("Списано бонусов: " + bonusPayment + ", новый бонусный баланс: " + newBonusBalance);
        }

        // СОЗДАЁМ СЕССИЮ (end_time = NULL для активной сессии)
        GameSession session = new GameSession();
        session.setProfileId(profile.getId());
        session.setTariffId(tariffId);
        session.setPromotionId(activePromotion != null ? activePromotion.getId() : null);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(null);  // ← ВАЖНО: NULL для активной сессии!
        session.setTotalCost(BigDecimal.valueOf(totalCost));
        session.setBonusEarned(BigDecimal.valueOf(bonusToEarn));
        session.setBonusUsed(BigDecimal.valueOf(bonusPayment));
        sessionDAO.create(session);

        System.out.println("Сессия создана: ID=" + session.getId() + ", startTime=" + session.getStartTime() + ", endTime=" + session.getEndTime());

        // Проверяем, что сессия сохранилась корректно
        GameSession checkSession = sessionDAO.findById(session.getId());
        System.out.println("Проверка: сессия ID=" + session.getId() + ", endTime=" + (checkSession != null ? checkSession.getEndTime() : "null"));

        return new Response("SUCCESS", "Сессия начата! Списано " + moneyPayment + " руб.", session.getId());
    }

    private Promotion getActivePromotion() {
        List<Promotion> promotions = promotionDAO.findAll();
        LocalDate today = LocalDate.now();
        for (Promotion p : promotions) {
            if (p.isActive() && p.getStartDate() != null && p.getEndDate() != null &&
                    !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate())) {
                return p;
            }
        }
        return null;
    }

    public Response endSession(int sessionId) {
        System.out.println("=== LoyaltyService.endSession ===");
        System.out.println("sessionId=" + sessionId);

        GameSession session = sessionDAO.findById(sessionId);
        if (session == null) {
            System.out.println("Сессия не найдена!");
            return new Response("ERROR", "Сессия не найдена");
        }

        System.out.println("Найдена сессия: startTime=" + session.getStartTime() + ", endTime=" + session.getEndTime());

        if (session.getEndTime() != null) {
            System.out.println("Сессия уже завершена!");
            return new Response("ERROR", "Сессия уже завершена");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        System.out.println("Установлен endTime=" + now);

        long minutes = ChronoUnit.MINUTES.between(session.getStartTime(), now);
        double hours = Math.max(minutes / 60.0, 0.5);
        System.out.println("Часов прошло: " + hours);

        Tariff tariff = tariffDAO.findById(session.getTariffId());
        BigDecimal totalCost = BigDecimal.valueOf(hours).multiply(tariff.getPricePerHour());
        BigDecimal bonusEarned = BigDecimal.valueOf(hours).multiply(BigDecimal.valueOf(tariff.getBonusPerHour()));

        System.out.println("totalCost=" + totalCost + ", bonusEarned=" + bonusEarned);

        session.setTotalCost(totalCost);
        session.setBonusEarned(bonusEarned);
        session.setBonusUsed(BigDecimal.ZERO);

        sessionDAO.update(session);

        GameSession checkSession = sessionDAO.findById(sessionId);
        System.out.println("Проверка после обновления: endTime=" + (checkSession != null ? checkSession.getEndTime() : "null"));

        ClientProfile profile = profileDAO.findById(session.getProfileId());
        if (profile != null) {
            BigDecimal newBonusBalance = profile.getBonusBalance().add(bonusEarned);
            profileDAO.updateBonusBalance(profile.getId(), newBonusBalance);
            profileDAO.updateTotalSpent(profile.getId(), totalCost);
            System.out.println("Баланс обновлён: +" + bonusEarned);
        }
        updateLoyaltyStatus(profile.getId(), profile.getTotalSpent());


        return new Response("SUCCESS", "Сессия завершена. Начислено " + bonusEarned + " бонусов", bonusEarned);
    }

    public Response redeemBonus(int userId, double amount) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }
        BigDecimal bonusAmount = BigDecimal.valueOf(amount);
        if (profile.getBonusBalance().compareTo(bonusAmount) < 0) {
            return new Response("ERROR", "Недостаточно бонусов");
        }
        BigDecimal newBalance = profile.getBonusBalance().subtract(bonusAmount);
        profileDAO.updateBonusBalance(profile.getId(), newBalance);
        return new Response("SUCCESS", "Списано " + amount + " бонусов", newBalance);
    }

    public Response getTariffs() {
        try {
            List<Tariff> tariffs = tariffDAO.findAll();
            List<TariffDTO> result = new ArrayList<>();
            for (Tariff t : tariffs) {
                TariffDTO dto = new TariffDTO();
                dto.setTariffId(t.getId());
                dto.setName(t.getName());
                dto.setPricePerHour(t.getPricePerHour().doubleValue());
                dto.setBonusPerHour(t.getBonusPerHour());
                dto.setActive(t.isActive());
                dto.setDescription(t.getDescription());
                result.add(dto);
            }
            return new Response("SUCCESS", "Тарифы получены", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("ERROR", "Ошибка: " + e.getMessage());
        }
    }

    public Response addTariff(String name, double price, double bonusPerHour) {
        Tariff tariff = new Tariff();
        tariff.setName(name);
        tariff.setPricePerHour(BigDecimal.valueOf(price));
        tariff.setBonusPerHour(bonusPerHour);
        tariff.setActive(true);
        tariffDAO.create(tariff);
        return new Response("SUCCESS", "Тариф добавлен");
    }

    public Response updateTariff(int tariffId, String name, double price, double bonusPerHour) {
        tariffDAO.update(tariffId, name, BigDecimal.valueOf(price), bonusPerHour);
        return new Response("SUCCESS", "Тариф обновлён");
    }

    public Response deleteTariff(int tariffId) {
        tariffDAO.delete(tariffId);
        return new Response("SUCCESS", "Тариф удалён");
    }

    public Response getPromotions() {
        List<Promotion> promotions = promotionDAO.findAll();
        List<PromotionDTO> result = new ArrayList<>();
        for (Promotion p : promotions) {
            PromotionDTO dto = new PromotionDTO();
            dto.setPromotionId(p.getId());
            dto.setName(p.getName());
            dto.setBonusMultiplier(p.getBonusMultiplier());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setActive(p.isActive());
            result.add(dto);
        }
        return new Response("SUCCESS", "Акции получены", result);
    }

    public Response addPromotion(String name, double multiplier, LocalDate startDate, LocalDate endDate) {
        Promotion promotion = new Promotion();
        promotion.setName(name);
        promotion.setBonusMultiplier(multiplier);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setActive(true);
        promotionDAO.create(promotion);
        return new Response("SUCCESS", "Акция добавлена");
    }

    public Response updatePromotion(int promotionId, String name, double multiplier) {
        promotionDAO.update(promotionId, name, multiplier);
        return new Response("SUCCESS", "Акция обновлена");
    }

    public Response deletePromotion(int promotionId) {
        promotionDAO.delete(promotionId);
        return new Response("SUCCESS", "Акция удалена");
    }

    public Response getAllUsers() {
        List<User> users = userDAO.findAll();
        List<UserDTO> result = new ArrayList<>();
        for (User u : users) {
            UserDTO dto = new UserDTO();
            dto.setUserId(u.getId());
            dto.setLogin(u.getLogin());
            dto.setFullName(u.getFullName() != null ? u.getFullName() : u.getLogin());
            dto.setRoleId(u.getRoleId());
            dto.setBlocked(u.isBlocked());

            // Добавляем информацию о статусе лояльности
            ClientProfile profile = profileDAO.findByUserId(u.getId());
            if (profile != null) {
                String statusSql = "SELECT status_name FROM loyaltystatuses WHERE status_id = ?";
                try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(statusSql)) {
                    stmt.setInt(1, profile.getStatusId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        dto.setLoyaltyStatus(rs.getString("status_name"));
                    }
                } catch (SQLException e) {
                    dto.setLoyaltyStatus("Новичок");
                }
            }

            result.add(dto);
        }
        return new Response("SUCCESS", "Пользователи получены", result);
    }

    public Response blockUser(int userId) {
        userDAO.updateBlocked(userId, true);
        return new Response("SUCCESS", "Пользователь заблокирован");
    }

    public Response unblockUser(int userId) {
        userDAO.updateBlocked(userId, false);
        return new Response("SUCCESS", "Пользователь разблокирован");
    }

    public Response updateUser(int userId, String newFullName) {
        userDAO.updateFullName(userId, newFullName);
        return new Response("SUCCESS", "Пользователь обновлён");
    }

    public Response deleteUser(int userId) {
        userDAO.delete(userId);
        return new Response("SUCCESS", "Пользователь удалён");
    }

    public Response addBonus(int userId, double amount) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Пользователь не найден");
        }
        BigDecimal newBalance = profile.getBonusBalance().add(BigDecimal.valueOf(amount));
        profileDAO.updateBonusBalance(profile.getId(), newBalance);
        return new Response("SUCCESS", "Бонусы добавлены. Новый баланс: " + newBalance);
    }

    public Response addMoney(int userId, double amount) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Пользователь не найден");
        }
        BigDecimal newBalance = profile.getMoneyBalance().add(BigDecimal.valueOf(amount));
        profileDAO.updateMoneyBalance(profile.getId(), newBalance);
        return new Response("SUCCESS", "Баланс пополнен. Новый баланс: " + newBalance);
    }

    public Response generateReport(LocalDate from, LocalDate to) {
        List<GameSession> sessions = sessionDAO.findByDateRange(from, to);
        double totalRevenue = 0;
        StringBuilder report = new StringBuilder();
        report.append("Отчёт за период ").append(from).append(" - ").append(to).append("\n");
        report.append("Всего сессий: ").append(sessions.size()).append("\n");
        for (GameSession s : sessions) {
            totalRevenue += s.getTotalCost().doubleValue();
            report.append("Сессия #").append(s.getId()).append(": ").append(s.getTotalCost()).append(" руб\n");
        }
        report.append("Общая выручка: ").append(totalRevenue).append(" руб");
        return new Response("SUCCESS", "Отчёт сформирован", report.toString());
    }

    /**
     * Обновление статуса лояльности клиента на основе общей суммы трат
     */
    private void updateLoyaltyStatus(int profileId, BigDecimal totalSpent) {
        int newStatusId = 1; // по умолчанию Новичок

        if (totalSpent.compareTo(BigDecimal.valueOf(500)) >= 0) {
            newStatusId = 3; // VIP
        } else if (totalSpent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            newStatusId = 2; // Постоянный
        }

        ClientProfile profile = profileDAO.findById(profileId);
        if (profile != null && profile.getStatusId() != newStatusId) {
            profileDAO.updateStatus(profileId, newStatusId);
            System.out.println("Статус клиента обновлён: profileId=" + profileId +
                    ", новый statusId=" + newStatusId);
        }
    }

    /**
     * Получение текущего статуса клиента с информацией о скидках и бонусах
     */
    public Response getClientStatus(int userId) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }

        // Получаем информацию о статусе
        String sql = "SELECT * FROM loyaltystatuses WHERE status_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, profile.getStatusId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("statusId", rs.getInt("status_id"));
                result.put("statusName", rs.getString("status_name"));
                result.put("discountPercent", rs.getDouble("discount_percent"));
                result.put("bonusMultiplier", rs.getDouble("bonus_multiplier"));
                result.put("minSpendForNext", getMinSpendForNextStatus(profile.getStatusId()));
                result.put("currentSpent", profile.getTotalSpent());
                return new Response("SUCCESS", "Статус получен", result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Response("ERROR", "Статус не найден");
    }

    /**
     * Получение минимальной суммы для следующего статуса
     */
    private double getMinSpendForNextStatus(int currentStatusId) {
        if (currentStatusId == 1) return 100;  // до Постоянного
        if (currentStatusId == 2) return 500;  // до VIP
        return 0; // уже максимальный статус
    }

    /**
     * Ручное изменение статуса клиента (администратором)
     */
    public Response updateClientStatus(int userId, int newStatusId) {
        ClientProfile profile = profileDAO.findByUserId(userId);
        if (profile == null) {
            return new Response("ERROR", "Профиль не найден");
        }

        // Проверка существования статуса
        String checkSql = "SELECT * FROM loyaltystatuses WHERE status_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(checkSql)) {
            stmt.setInt(1, newStatusId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new Response("ERROR", "Статус с ID=" + newStatusId + " не существует");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response("ERROR", "Ошибка проверки статуса");
        }

        profileDAO.updateStatus(profile.getId(), newStatusId);
        return new Response("SUCCESS", "Статус клиента обновлён на ID=" + newStatusId);
    }
}


