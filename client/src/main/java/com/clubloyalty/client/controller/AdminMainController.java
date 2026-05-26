package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.util.SessionManager;
import com.clubloyalty.common.dto.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class AdminMainController {

    @FXML private Text userNameLabel;
    @FXML private StackPane contentArea;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        UserDTO user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText("👤 " + user.getLogin());
        }
        handleShowUsers();
    }

    @FXML
    private void handleShowUsers() {
        loadContent("AdminUsers.fxml");
    }

    @FXML
    private void handleShowRegister() {
        loadContent("AdminRegister.fxml");
    }

    @FXML
    private void handleShowTariffs() {
        loadContent("AdminTariffs.fxml");
    }

    @FXML
    private void handleShowPromotions() {
        loadContent("AdminPromotions.fxml");
    }

    @FXML
    private void handleShowBonuses() {
        loadContent("AdminBonuses.fxml");
    }

    @FXML
    private void handleShowReports() {
        loadContent("AdminReports.fxml");
    }

    @FXML
    private void handleLogout() throws Exception {
        SessionManager.getInstance().logout();
        mainApp.showLoginWindow();
    }

    private void loadContent(String fxmlFile) {
        try {
            System.out.println("Загрузка: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка загрузки " + fxmlFile + ": " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}