package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.dto.TariffDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Optional;

public class AdminTariffsController {

    @FXML private TableView<TariffDTO> tariffsTable;
    @FXML private TableColumn<TariffDTO, Integer> colTariffId;
    @FXML private TableColumn<TariffDTO, String> colTariffName;
    @FXML private TableColumn<TariffDTO, Double> colTariffPrice;
    @FXML private TableColumn<TariffDTO, Double> colBonusPerHour;
    @FXML private TextField tariffNameField;
    @FXML private TextField tariffPriceField;
    @FXML private TextField tariffBonusField;
    @FXML private Label messageLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        colTariffId.setCellValueFactory(new PropertyValueFactory<>("tariffId"));
        colTariffName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTariffPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        colBonusPerHour.setCellValueFactory(new PropertyValueFactory<>("bonusPerHour"));
        loadTariffs();
    }

    private void loadTariffs() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_TARIFFS);
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                List<TariffDTO> tariffs = (List<TariffDTO>) response.getData();
                tariffsTable.setItems(FXCollections.observableArrayList(tariffs));
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (Exception e) {
            messageLabel.setText("Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTariff() {
        String name = tariffNameField.getText();
        String priceText = tariffPriceField.getText();
        String bonusText = tariffBonusField.getText();

        if (name.isEmpty() || priceText.isEmpty() || bonusText.isEmpty()) {
            messageLabel.setText("Заполните все поля");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            double bonus = Double.parseDouble(bonusText);
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_TARIFF, new Object[]{name, price, bonus});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                messageLabel.setText("Тариф добавлен!");
                tariffNameField.clear();
                tariffPriceField.clear();
                tariffBonusField.clear();
                loadTariffs();
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Введите корректные числа");
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTariff() {
        TariffDTO selected = tariffsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите тариф для редактирования");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setTitle("Редактирование тарифа");
        nameDialog.setHeaderText("Редактирование тарифа ID: " + selected.getTariffId());
        nameDialog.setContentText("Новое название:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isPresent()) {
            String newName = nameResult.get();

            TextInputDialog priceDialog = new TextInputDialog(String.valueOf(selected.getPricePerHour()));
            priceDialog.setTitle("Редактирование тарифа");
            priceDialog.setContentText("Новая цена:");

            Optional<String> priceResult = priceDialog.showAndWait();
            if (priceResult.isPresent()) {
                try {
                    double newPrice = Double.parseDouble(priceResult.get());

                    TextInputDialog bonusDialog = new TextInputDialog(String.valueOf(selected.getBonusPerHour()));
                    bonusDialog.setTitle("Редактирование тарифа");
                    bonusDialog.setContentText("Новые бонусы в час:");

                    Optional<String> bonusResult = bonusDialog.showAndWait();
                    if (bonusResult.isPresent()) {
                        double newBonus = Double.parseDouble(bonusResult.get());
                        ClientSocket socket = ClientSocket.getInstance();
                        Request request = new Request(ActionType.UPDATE_TARIFF, new Object[]{selected.getTariffId(), newName, newPrice, newBonus});
                        Response response = socket.sendRequest(request);

                        if (response.getStatus().equals("SUCCESS")) {
                            messageLabel.setText("Тариф обновлён!");
                            loadTariffs();
                        } else {
                            messageLabel.setText("Ошибка: " + response.getMessage());
                        }
                    }
                } catch (NumberFormatException e) {
                    messageLabel.setText("Введите корректные числа");
                } catch (Exception e) {
                    messageLabel.setText("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleDeleteTariff() {
        TariffDTO selected = tariffsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите тариф для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить тариф \"" + selected.getName() + "\"?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                ClientSocket socket = ClientSocket.getInstance();
                Request request = new Request(ActionType.DELETE_TARIFF, selected.getTariffId());
                Response response = socket.sendRequest(request);

                if (response.getStatus().equals("SUCCESS")) {
                    messageLabel.setText("Тариф удалён!");
                    loadTariffs();
                } else {
                    messageLabel.setText("Ошибка: " + response.getMessage());
                }
            } catch (Exception e) {
                messageLabel.setText("Ошибка: " + e.getMessage());
            }
        }
    }
}