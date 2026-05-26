package com.clubloyalty.server.service;

import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import com.clubloyalty.server.dao.ClientProfileDAO;
import com.clubloyalty.server.dao.UserDAO;
import com.clubloyalty.server.interfaces.ServiceInterface;
import com.clubloyalty.server.model.ClientProfile;
import com.clubloyalty.server.model.User;
import java.math.BigDecimal;

public class AuthService implements ServiceInterface {
    private final UserDAO userDAO;
    private final ClientProfileDAO profileDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.profileDAO = new ClientProfileDAO();
    }

    @Override
    public Response execute(Request request) {
        return null;
    }

    public Response login(String login, String password) {
        System.out.println("=== ПОПЫТКА ВХОДА ===");
        System.out.println("Логин: " + login);
        System.out.println("Пароль: " + password);
        if (login == null || login.trim().isEmpty()) {
            return new Response("ERROR", "Логин не может быть пустым");
        }
        if (password == null || password.trim().isEmpty()) {
            return new Response("ERROR", "Пароль не может быть пустым");
        }

        User user = userDAO.findByLogin(login);

        if (user == null) {
            return new Response("ERROR", "Пользователь не найден");
        }

        if (user.isBlocked()) {
            return new Response("ERROR", "Пользователь заблокирован");
        }

        if (!user.getPasswordHash().equals(password)) {
            return new Response("ERROR", "Неверный пароль");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setLogin(user.getLogin());
        userDTO.setFullName(user.getFullName() != null ? user.getFullName() : user.getLogin());
        userDTO.setRoleId(user.getRoleId());
        userDTO.setBonusBalance(0);
        userDTO.setLoyaltyStatus("Новичок");

        return new Response("SUCCESS", "Вход выполнен успешно", userDTO);
    }

    public Response register(String login, String password, String fullName, String phone, String email) {
        System.out.println("=== НАЧАЛО РЕГИСТРАЦИИ ===");
        System.out.println("Логин: " + login);


        if (login == null || login.trim().isEmpty()) {
            return new Response("ERROR", "Логин не может быть пустым");
        }
        if (password == null || password.trim().isEmpty()) {
            return new Response("ERROR", "Пароль не может быть пустым");
        }

        try {
            User existingUser = userDAO.findByLogin(login);
            if (existingUser != null) {
                return new Response("ERROR", "Пользователь с таким логином уже существует");
            }

            User user = new User();
            user.setLogin(login);
            user.setPasswordHash(password);
            user.setFullName(fullName != null && !fullName.isEmpty() ? fullName : login);
            user.setPhone(phone != null ? phone : "");
            user.setEmail(email != null ? email : "");
            user.setRoleId(2);
            user.setActive(true);
            user.setBlocked(false);

            boolean success = userDAO.create(user);

            if (success) {
                User createdUser = userDAO.findByLogin(login);
                ClientProfile profile = new ClientProfile();
                profile.setUserId(createdUser.getId());
                profile.setBonusBalance(BigDecimal.valueOf(100));
                profile.setTotalSpent(BigDecimal.ZERO);
                profile.setStatusId(1);
                profileDAO.create(profile);

                return new Response("SUCCESS", "Регистрация прошла успешно. Вам начислено 100 бонусов!");
            } else {
                return new Response("ERROR", "Ошибка при создании пользователя");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response("ERROR", "Ошибка: " + e.getMessage());
        }
    }

    private String getLoyaltyStatus(ClientProfile profile) {
        if (profile == null) return "Новичок";
        int statusId = profile.getStatusId();
        if (statusId == 3) return "VIP";
        if (statusId == 2) return "Постоянный";
        return "Новичок";
    }
}