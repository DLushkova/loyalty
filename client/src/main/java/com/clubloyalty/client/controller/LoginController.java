package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.client.util.SessionManager;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button guestBtn;
    @FXML private Text errorLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля");
            errorLabel.setVisible(true);
            return;
        }

        try {
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }

            Request request = new Request(ActionType.LOGIN, new String[]{login, password});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                UserDTO user = (UserDTO) response.getData();
                SessionManager.getInstance().setCurrentUser(user);

                // Проверяем роль и открываем соответствующее окно
                if (user.getRoleId() == 1) {
                    // Администратор → открываем админ панель
                    mainApp.showAdminMainWindow();  // ← ИСПРАВЛЕНО
                } else {
                    // Обычный клиент → открываем главное окно
                    mainApp.showMainWindow();
                }
            } else {
                errorLabel.setText(response.getMessage());
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleGuestLogin() throws Exception {
        UserDTO guest = new UserDTO();
        guest.setUserId(0);
        guest.setLogin("guest");
        guest.setFullName("Гость");
        guest.setRoleId(3);
        guest.setBonusBalance(0);

        SessionManager.getInstance().setCurrentUser(guest);
        mainApp.showMainWindow();
    }

    @FXML
    private void handleRegister() throws Exception {
        mainApp.showRegisterWindow();
    }
}