package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.clubloyalty.common.network.Response;

public class AdminBonusesController {

    @FXML private TextField userIdField;
    @FXML private TextField moneyAmountField;
    @FXML private Label messageLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleAddMoney() {
        try {
            int userId = Integer.parseInt(userIdField.getText());
            double amount = Double.parseDouble(moneyAmountField.getText());

            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_MONEY, new Object[]{userId, amount});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                messageLabel.setText("Баланс пополнен на " + amount + " руб!");
                userIdField.clear();
                moneyAmountField.clear();
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Введите корректные данные");
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddBonus() {
        try {
            int userId = Integer.parseInt(userIdField.getText());
            double amount = 10.0; // фиксированная сумма для теста

            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_BONUS, new Object[]{userId, amount});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                messageLabel.setText("Бонусы начислены: " + amount);
                userIdField.clear();
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Введите корректный ID пользователя");
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }
}