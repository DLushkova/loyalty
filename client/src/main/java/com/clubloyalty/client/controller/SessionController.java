package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.client.util.SessionManager;
import com.clubloyalty.common.dto.PromotionDTO;
import com.clubloyalty.common.dto.TariffDTO;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.util.List;

public class SessionController {

    @FXML private ComboBox<TariffDTO> tariffCombo;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private RadioButton payMoneyBtn;
    @FXML private RadioButton payBonusBtn;
    @FXML private Text totalCostText;
    @FXML private Text bonusToEarnText;
    @FXML private Text promotionInfoText;
    @FXML private Label errorLabel;

    private Main mainApp;
    private List<PromotionDTO> promotions;
    private TariffDTO selectedTariff;
    private double bonusBalance = 0;
    private double moneyBalance = 0;
    private ToggleGroup paymentGroup;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 1));

        paymentGroup = new ToggleGroup();
        payMoneyBtn.setToggleGroup(paymentGroup);
        payBonusBtn.setToggleGroup(paymentGroup);
        payMoneyBtn.setSelected(true);

        loadTariffs();
        loadPromotions();
        loadBalances();

        tariffCombo.valueProperty().addListener((obs, old, val) -> {
            selectedTariff = val;
            updateCalculation();
        });

        hoursSpinner.valueProperty().addListener((obs, old, val) -> updateCalculation());
        paymentGroup.selectedToggleProperty().addListener((obs, old, val) -> updateCalculation());
    }

    private void loadTariffs() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_TARIFFS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                List<TariffDTO> tariffs = (List<TariffDTO>) response.getData();
                tariffCombo.setCellFactory(lv -> new ListCell<TariffDTO>() {
                    @Override
                    protected void updateItem(TariffDTO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " (" + item.getPricePerHour() + " руб/час, +" + item.getBonusPerHour() + " бонусов/час)");
                        }
                    }
                });
                tariffCombo.setButtonCell(new ListCell<TariffDTO>() {
                    @Override
                    protected void updateItem(TariffDTO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                });
                tariffCombo.getItems().setAll(tariffs);
                if (!tariffs.isEmpty()) {
                    tariffCombo.getSelectionModel().selectFirst();
                    selectedTariff = tariffs.get(0);
                    updateCalculation();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки тарифов: " + e.getMessage());
        }
    }

    private void loadPromotions() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_PROMOTIONS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                promotions = (List<PromotionDTO>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBalances() {
        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            if (user == null) return;
            ClientSocket socket = ClientSocket.getInstance();

            Request bonusReq = new Request(ActionType.GET_BONUS_BALANCE, user.getUserId());
            Response bonusRes = socket.sendRequest(bonusReq);
            if (bonusRes.getStatus().equals("SUCCESS")) {
                bonusBalance = ((Number) bonusRes.getData()).doubleValue();
            }

            Request moneyReq = new Request(ActionType.GET_MONEY_BALANCE, user.getUserId());
            Response moneyRes = socket.sendRequest(moneyReq);
            if (moneyRes.getStatus().equals("SUCCESS")) {
                moneyBalance = ((Number) moneyRes.getData()).doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCalculation() {
        if (selectedTariff == null) return;

        int hours = hoursSpinner.getValue();
        double total = selectedTariff.getPricePerHour() * hours;
        double bonusToEarn = selectedTariff.getBonusPerHour() * hours;

        PromotionDTO activePromotion = getActivePromotion();
        if (activePromotion != null) {
            bonusToEarn = bonusToEarn * activePromotion.getBonusMultiplier();
            promotionInfoText.setText("Акция: " + activePromotion.getName() + " (x" + activePromotion.getBonusMultiplier() + ")");
        } else {
            promotionInfoText.setText("Нет активных акций");
        }

        totalCostText.setText(String.format("%.2f руб", total));
        bonusToEarnText.setText(String.format("%.0f бонусов", bonusToEarn));

        boolean payWithBonus = payBonusBtn.isSelected();
        if (payWithBonus) {
            double maxBonusPayment = total * 0.5;
            if (bonusBalance < maxBonusPayment) {
                errorLabel.setText("Недостаточно бонусов! Доступно: " + bonusBalance);
            } else {
                errorLabel.setText("");
            }
        } else {
            if (moneyBalance < total) {
                errorLabel.setText("Недостаточно средств! Доступно: " + moneyBalance);
            } else {
                errorLabel.setText("");
            }
        }
    }

    private PromotionDTO getActivePromotion() {
        if (promotions == null) return null;
        LocalDate today = LocalDate.now();
        for (PromotionDTO p : promotions) {
            if (p.isActive() && p.getStartDate() != null && p.getEndDate() != null &&
                    !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate())) {
                return p;
            }
        }
        return null;
    }

    @FXML
    private void handleStartSession() {
        if (selectedTariff == null) {
            errorLabel.setText("Выберите тариф");
            return;
        }

        int hours = hoursSpinner.getValue();
        double total = selectedTariff.getPricePerHour() * hours;
        boolean payWithBonus = payBonusBtn.isSelected();

        if (payWithBonus) {
            double maxBonusPayment = total * 0.5;
            if (bonusBalance < maxBonusPayment) {
                errorLabel.setText("Недостаточно бонусов");
                return;
            }
        } else {
            if (moneyBalance < total) {
                errorLabel.setText("Недостаточно средств");
                return;
            }
        }

        try {
            UserDTO user = SessionManager.getInstance().getCurrentUser();
            ClientSocket socket = ClientSocket.getInstance();
            if (!socket.isConnected()) {
                socket.connect("localhost", 8080);
            }
            Request request = new Request(ActionType.START_SESSION,
                    new Object[]{user.getUserId(), selectedTariff.getTariffId(), hours, payWithBonus});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Сессия начата!");
                alert.showAndWait();
                closeWindow();

                // Принудительное обновление главного окна
                if (mainApp != null) {
                    Thread.sleep(500);
                    mainApp.refreshMainWindow();
                }
            } else {
                errorLabel.setText(response.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        tariffCombo.getScene().getWindow().hide();
    }
}