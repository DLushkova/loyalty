package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.client.util.SessionManager;
import com.clubloyalty.common.dto.PromotionDTO;
import com.clubloyalty.common.dto.SessionDTO;
import com.clubloyalty.common.dto.TariffDTO;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class MainController {

    @FXML private Text welcomeLabel;
    @FXML private Button logoutBtn;
    @FXML private Text bonusBalanceText;
    @FXML private Text moneyBalanceText;
    @FXML private Text remainingTimeText;
    @FXML private HBox actionButtons;
    @FXML private Button adminPanelBtn;
    @FXML private TableView<SessionDTO> sessionsTable;
    @FXML private TabPane tabPane;
    @FXML private Tab mainTab;
    @FXML private Tab loginTab;
    @FXML private Text statusText;
    @FXML private Text discountText;
    @FXML private Text bonusMultiplierText;
    @FXML private ProgressBar progressBar;
    @FXML private Text nextStatusText;

    @FXML private TableView<TariffDTO> tariffsTable;
    @FXML private TableColumn<TariffDTO, Integer> colTariffId;
    @FXML private TableColumn<TariffDTO, String> colTariffName;
    @FXML private TableColumn<TariffDTO, Double> colTariffPrice;
    @FXML private TableColumn<TariffDTO, Double> colTariffBonus;

    @FXML private TableView<PromotionDTO> promotionsTable;
    @FXML private TableColumn<PromotionDTO, Integer> colPromoId;
    @FXML private TableColumn<PromotionDTO, String> colPromoName;
    @FXML private TableColumn<PromotionDTO, Double> colPromoMultiplier;

    private Main mainApp;
    private Timeline timer;
    private int activeSessionId = -1;
    private LocalDateTime sessionEndTime;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        setupTables();
        loadTariffs();
        loadPromotions();
        updateUI();
        startTimer();

        // Добавляем слушатель на выбор вкладки "ГЛАВНАЯ" для автоматического обновления статуса
        mainTab.setOnSelectionChanged(event -> {
            if (mainTab.isSelected() && SessionManager.getInstance().getCurrentUser() != null) {
                refreshAllData();
            }
        });
    }

    // Новый метод для обновления всех данных клиента
    @FXML
    public void refreshAllData() {
        System.out.println("=== refreshAllData ===");
        loadClientStatus();
        loadBonusBalance();
        loadMoneyBalance();
        loadSessionsHistory();
        checkActiveSession();
    }

    private void setupTables() {
        colTariffId.setCellValueFactory(new PropertyValueFactory<>("tariffId"));
        colTariffName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTariffPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        colTariffBonus.setCellValueFactory(new PropertyValueFactory<>("bonusPerHour"));

        colPromoId.setCellValueFactory(new PropertyValueFactory<>("promotionId"));
        colPromoName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPromoMultiplier.setCellValueFactory(new PropertyValueFactory<>("bonusMultiplier"));

        TableColumn<SessionDTO, String> dateCol = new TableColumn<>("Дата");
        TableColumn<SessionDTO, String> tariffCol = new TableColumn<>("Тариф");
        TableColumn<SessionDTO, Number> costCol = new TableColumn<>("Стоимость");
        TableColumn<SessionDTO, Number> bonusCol = new TableColumn<>("Бонусы");

        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartTime() != null ?
                                cellData.getValue().getStartTime().toString() : ""));

        tariffCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTariffName() != null ?
                                cellData.getValue().getTariffName() : ""));

        costCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalCost()));

        bonusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBonusEarned()));

        sessionsTable.getColumns().setAll(dateCol, tariffCol, costCol, bonusCol);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        UserDTO user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Добро пожаловать, " + user.getFullName());
            loadBonusBalance();
            loadMoneyBalance();
            loadClientStatus();
        }
    }

    private void loadClientStatus() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getUserId() == 0) return;

            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }

            Request request = new Request(ActionType.GET_CLIENT_STATUS, user.getUserId());
            Response response = socket.sendRequest(request);

            System.out.println("loadClientStatus: status=" + response.getStatus());
            System.out.println("loadClientStatus: message=" + response.getMessage());

            if (response.getStatus().equals("SUCCESS")) {
                Map<String, Object> data = (Map<String, Object>) response.getData();

                // ИСПРАВЛЕНО: безопасное преобразование Number (Integer, BigDecimal, Double) в double
                String statusName = (String) data.get("statusName");
                double discountPercent = ((Number) data.get("discountPercent")).doubleValue();
                double bonusMultiplier = ((Number) data.get("bonusMultiplier")).doubleValue();
                double currentSpent = ((Number) data.get("currentSpent")).doubleValue();
                double minSpendForNext = ((Number) data.get("minSpendForNext")).doubleValue();

                System.out.println("Статус: " + statusName + ", скидка: " + discountPercent + "%, множитель: " + bonusMultiplier);

                statusText.setText(statusName);
                discountText.setText("Скидка: " + discountPercent + "%");
                bonusMultiplierText.setText("Бонус x" + bonusMultiplier);

                // Прогресс до следующего статуса
                if (minSpendForNext > 0 && !"VIP".equals(statusName)) {
                    double progress = currentSpent / minSpendForNext;
                    if (progress > 1) progress = 1;
                    progressBar.setProgress(progress);
                    double remaining = minSpendForNext - currentSpent;
                    nextStatusText.setText("До " + getNextStatusName(statusName) + ": " +
                            Math.round(remaining * 100) / 100.0 + " руб");
                } else if ("VIP".equals(statusName)) {
                    progressBar.setProgress(1.0);
                    nextStatusText.setText("🏆 Вы достигли максимального статуса!");
                } else {
                    progressBar.setProgress(0);
                    nextStatusText.setText("");
                }
            } else {
                System.out.println("Ошибка загрузки статуса: " + response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBonusBalance() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getUserId() == 0) return;
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.GET_BONUS_BALANCE, user.getUserId());
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                // ИСПРАВЛЕНО: безопасное преобразование Number в double
                double balance = ((Number) response.getData()).doubleValue();
                bonusBalanceText.setText(balance + " ₽");
                System.out.println("Бонусный баланс: " + balance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMoneyBalance() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getUserId() == 0) return;
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.GET_MONEY_BALANCE, user.getUserId());
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                // ИСПРАВЛЕНО: безопасное преобразование Number в double
                double balance = ((Number) response.getData()).doubleValue();
                moneyBalanceText.setText(balance + " ₽");
                System.out.println("Денежный баланс: " + balance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSessionsHistory() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getUserId() == 0) return;
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.GET_MY_SESSIONS, user.getUserId());
            Response response = socket.sendRequest(request);
            System.out.println("loadSessionsHistory: status=" + response.getStatus());

            if (response.getStatus().equals("SUCCESS")) {
                List<SessionDTO> sessions = (List<SessionDTO>) response.getData();
                System.out.println("Загружено сессий: " + (sessions != null ? sessions.size() : 0));
                if (sessions != null) {
                    sessionsTable.setItems(FXCollections.observableArrayList(sessions));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRemainingTime()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void checkActiveSession() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getUserId() == 0) return;

            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.GET_ACTIVE_SESSION, user.getUserId());
            Response response = socket.sendRequest(request);
            System.out.println("checkActiveSession: status=" + response.getStatus());

            if (response.getStatus().equals("SUCCESS") && response.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                activeSessionId = ((Number) data.get("sessionId")).intValue();
                System.out.println("activeSessionId=" + activeSessionId);
                String endTimeStr = (String) data.get("endTime");
                if (endTimeStr != null) {
                    sessionEndTime = LocalDateTime.parse(endTimeStr);
                    remainingTimeText.setVisible(true);
                    remainingTimeText.setText("⏱ Активная сессия!");
                }
            } else {
                activeSessionId = -1;
                sessionEndTime = null;
                remainingTimeText.setText("⏸ Нет активной сессии");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRemainingTime() {
        if (sessionEndTime != null) {
            java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), sessionEndTime);
            if (duration.isNegative() || duration.isZero()) {
                if (activeSessionId != -1) {
                    handleEndSession();
                }
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;
                remainingTimeText.setText(String.format("🎮 Активная сессия! Осталось: %02d:%02d:%02d", hours, minutes, seconds));
            }
        }
    }

    @FXML
    private void handleStartGame() throws Exception {
        mainApp.showSessionWindow();
        Thread.sleep(500);
        refreshAllData();
    }

    @FXML
    private void handleEndSession() {
        System.out.println("=== handleEndSession ===");
        System.out.println("activeSessionId=" + activeSessionId);

        checkActiveSession();
        System.out.println("После checkActiveSession: activeSessionId=" + activeSessionId);

        if (activeSessionId == -1) {
            showAlert("Нет активной сессии");
            return;
        }

        try {
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.END_SESSION, activeSessionId);
            System.out.println("Отправка END_SESSION с ID=" + activeSessionId);
            Response response = socket.sendRequest(request);
            System.out.println("Ответ: " + response.getStatus() + " - " + response.getMessage());

            Alert alert = new Alert(response.getStatus().equals("SUCCESS") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(response.getStatus().equals("SUCCESS") ? "Успех" : "Ошибка");
            alert.setContentText(response.getMessage());
            alert.showAndWait();

            if (response.getStatus().equals("SUCCESS")) {
                activeSessionId = -1;
                sessionEndTime = null;
                remainingTimeText.setText("⏸ Нет активной сессии");
                refreshAllData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdminPanel() throws Exception {
        mainApp.showAdminMainWindow();
    }

    @FXML
    private void handleLogout() throws Exception {
        if (timer != null) timer.stop();
        SessionManager.getInstance().logout();
        mainApp.showLoginWindow();
    }

    @FXML
    private void handleLogin() throws Exception {
        mainApp.showLoginWindow();
    }

    @FXML
    private void handleRegister() throws Exception {
        mainApp.showRegisterWindow();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getNextStatusName(String currentStatus) {
        switch (currentStatus) {
            case "Новичок":
                return "Постоянный";
            case "Постоянный":
                return "VIP";
            default:
                return "";
        }
    }
}