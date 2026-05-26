package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class RegisterController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private Text errorLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String fullName = fullNameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        System.out.println("Регистрация: логин=" + login);

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните логин и пароль");
            errorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Пароли не совпадают");
            errorLabel.setVisible(true);
            return;
        }

        try {
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }

            Object[] data = {login, password, fullName, phone, email};
            Request request = new Request(ActionType.REGISTER, data);
            Response response = socket.sendRequest(request);

            System.out.println("Ответ сервера: " + response.getStatus() + " - " + response.getMessage());

            if (response.getStatus().equals("SUCCESS")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText(response.getMessage());
                alert.showAndWait();
                mainApp.showLoginWindow();
            } else {
                errorLabel.setText(response.getMessage());
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() throws Exception {
        mainApp.showLoginWindow();
    }
}