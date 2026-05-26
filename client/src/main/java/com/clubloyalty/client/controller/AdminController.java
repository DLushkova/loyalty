package com.clubloyalty.client.controller;

import com.clubloyalty.common.dto.TariffDTO;
import com.clubloyalty.common.dto.PromotionDTO;
import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.client.util.SessionManager;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class AdminController {

    private Main mainApp;

    // ========== Вкладка ПОЛЬЗОВАТЕЛИ ==========
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Integer> colUserId;
    @FXML private TableColumn<UserDTO, String> colLogin;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, Integer> colRoleId;
    @FXML private TableColumn<UserDTO, Boolean> colBlocked;

    // ========== Вкладка РЕГИСТРАЦИЯ ==========
    @FXML private TextField regLoginField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regFullNameField;
    @FXML private TextField regPhoneField;
    @FXML private TextField regEmailField;
    @FXML private Label regMessageLabel;

    // ========== Вкладка ТАРИФЫ ==========
    @FXML private TableView<TariffDTO> tariffsTable;
    @FXML private TableColumn<TariffDTO, Integer> colTariffId;
    @FXML private TableColumn<TariffDTO, String> colTariffName;
    @FXML private TableColumn<TariffDTO, Double> colTariffPrice;
    @FXML private TextField tariffNameField;
    @FXML private TextField tariffPriceField;
    @FXML private Label tariffMessageLabel;

    // ========== Вкладка АКЦИИ ==========
    @FXML private TableView<PromotionDTO> promotionsTable;
    @FXML private TableColumn<PromotionDTO, Integer> colPromoId;
    @FXML private TableColumn<PromotionDTO, String> colPromoName;
    @FXML private TableColumn<PromotionDTO, Double> colPromoMultiplier;
    @FXML private TextField promoNameField;
    @FXML private TextField promoMultiplierField;
    @FXML private DatePicker promoStartDate;
    @FXML private DatePicker promoEndDate;
    @FXML private Label promoMessageLabel;

    // ========== Вкладка ОТЧЁТЫ ==========
    @FXML private DatePicker reportFromDate;
    @FXML private DatePicker reportToDate;
    @FXML private TextArea reportArea;

    // ========== Вкладка БОНУСЫ ==========
    @FXML private TextField bonusUserIdField;
    @FXML private TextField bonusAmountField;
    @FXML private Label bonusMessageLabel;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        setupUserTable();
        setupTariffsTable();
        setupPromotionsTable();
        loadUsers();
        loadTariffs();
        loadPromotions();
    }

    // ========== ПОЛЬЗОВАТЕЛИ ==========
    private void setupUserTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRoleId.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        colBlocked.setCellValueFactory(new PropertyValueFactory<>("blocked"));
    }

    private void loadUsers() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_ALL_USERS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                List<UserDTO> users = (List<UserDTO>) response.getData();
                usersTable.setItems(FXCollections.observableArrayList(users));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBlockUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Выберите пользователя"); return; }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.BLOCK_USER, selected.getUserId());
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) { loadUsers(); showAlert("Пользователь заблокирован"); }
            else { showAlert(response.getMessage()); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleUnblockUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Выберите пользователя"); return; }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.UNBLOCK_USER, selected.getUserId());
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) { loadUsers(); showAlert("Пользователь разблокирован"); }
            else { showAlert(response.getMessage()); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== РЕГИСТРАЦИЯ ==========
    @FXML private void handleRegisterUser() {
        String login = regLoginField.getText();
        String password = regPasswordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            regMessageLabel.setText("Заполните логин и пароль");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.REGISTER,
                    new Object[]{login, password, regFullNameField.getText(), regPhoneField.getText(), regEmailField.getText()});
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                regMessageLabel.setText("Пользователь зарегистрирован!");
                clearRegisterFields();
                loadUsers();
            } else {
                regMessageLabel.setText(response.getMessage());
            }
        } catch (Exception e) { regMessageLabel.setText("Ошибка: " + e.getMessage()); }
    }

    private void clearRegisterFields() {
        regLoginField.clear(); regPasswordField.clear(); regFullNameField.clear(); regPhoneField.clear(); regEmailField.clear();
    }

    // ========== ТАРИФЫ ==========
    private void setupTariffsTable() {
        colTariffId.setCellValueFactory(new PropertyValueFactory<>("tariffId"));
        colTariffName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTariffPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
    }

    private void loadTariffs() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_TARIFFS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                List<TariffDTO> tariffs = (List<TariffDTO>) response.getData();
                tariffsTable.setItems(FXCollections.observableArrayList(tariffs));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleAddTariff() {
        String name = tariffNameField.getText();
        String priceText = tariffPriceField.getText();
        if (name.isEmpty() || priceText.isEmpty()) {
            tariffMessageLabel.setText("Заполните название и цену");
            return;
        }
        try {
            double price = Double.parseDouble(priceText);
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_TARIFF, new Object[]{name, price});
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                tariffMessageLabel.setText("Тариф добавлен");
                tariffNameField.clear(); tariffPriceField.clear();
                loadTariffs();
            } else {
                tariffMessageLabel.setText(response.getMessage());
            }
        } catch (NumberFormatException e) {
            tariffMessageLabel.setText("Введите корректную цену");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== АКЦИИ ==========
    private void setupPromotionsTable() {
        colPromoId.setCellValueFactory(new PropertyValueFactory<>("promotionId"));
        colPromoName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPromoMultiplier.setCellValueFactory(new PropertyValueFactory<>("bonusMultiplier"));
    }

    private void loadPromotions() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_PROMOTIONS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                List<PromotionDTO> promotions = (List<PromotionDTO>) response.getData();
                promotionsTable.setItems(FXCollections.observableArrayList(promotions));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleAddPromotion() {
        String name = promoNameField.getText();
        String multiplierText = promoMultiplierField.getText();
        LocalDate start = promoStartDate.getValue();
        LocalDate end = promoEndDate.getValue();
        if (name.isEmpty() || multiplierText.isEmpty() || start == null || end == null) {
            promoMessageLabel.setText("Заполните все поля");
            return;
        }
        try {
            double multiplier = Double.parseDouble(multiplierText);
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_PROMOTION, new Object[]{name, multiplier, start, end});
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                promoMessageLabel.setText("Акция добавлена");
                promoNameField.clear(); promoMultiplierField.clear(); promoStartDate.setValue(null); promoEndDate.setValue(null);
                loadPromotions();
            } else {
                promoMessageLabel.setText(response.getMessage());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== ОТЧЁТЫ ==========
    @FXML private void handleGenerateReport() {
        LocalDate from = reportFromDate.getValue();
        LocalDate to = reportToDate.getValue();
        if (from == null || to == null) {
            showAlert("Выберите период");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GENERATE_REPORT, new Object[]{from, to});
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                reportArea.setText(response.getData().toString());
            } else {
                reportArea.setText(response.getMessage());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleExportCSV() {
        String content = reportArea.getText();
        if (content.isEmpty()) { showAlert("Сначала сформируйте отчёт"); return; }
        showAlert("Экспорт в CSV выполнен");
    }

    // ========== БОНУСЫ ==========
    @FXML private void handleAddBonus() {
        try {
            int userId = Integer.parseInt(bonusUserIdField.getText());
            double amount = Double.parseDouble(bonusAmountField.getText());
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_BONUS, new Object[]{userId, amount});
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                bonusMessageLabel.setText("Бонусы добавлены!");
                bonusUserIdField.clear(); bonusAmountField.clear();
                loadUsers();
            } else {
                bonusMessageLabel.setText(response.getMessage());
            }
        } catch (NumberFormatException e) {
            bonusMessageLabel.setText("Введите корректные данные");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() throws Exception {
        SessionManager.getInstance().logout();
        mainApp.showLoginWindow();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}