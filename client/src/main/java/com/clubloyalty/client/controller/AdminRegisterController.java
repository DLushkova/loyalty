package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminRegisterController {

    @FXML private TextField regLoginField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regFullNameField;
    @FXML private TextField regPhoneField;
    @FXML private TextField regEmailField;
    @FXML private Label messageLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() {
        String login = regLoginField.getText();
        String password = regPasswordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Заполните логин и пароль");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.REGISTER,
                    new Object[]{login, password, regFullNameField.getText(), regPhoneField.getText(), regEmailField.getText()});
            socket.sendRequest(request);
            messageLabel.setText("Пользователь зарегистрирован!");
            clearFields();
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    private void clearFields() {
        regLoginField.clear();
        regPasswordField.clear();
        regFullNameField.clear();
        regPhoneField.clear();
        regEmailField.clear();
    }
}