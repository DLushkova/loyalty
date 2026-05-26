package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.dto.PromotionDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminPromotionsController {

    @FXML private TableView<PromotionDTO> promotionsTable;
    @FXML private TableColumn<PromotionDTO, Integer> colPromoId;
    @FXML private TableColumn<PromotionDTO, String> colPromoName;
    @FXML private TableColumn<PromotionDTO, Double> colPromoMultiplier;

    @FXML private TextField searchField;
    @FXML private TextField promoNameField;
    @FXML private TextField promoMultiplierField;
    @FXML private DatePicker promoStartDate;
    @FXML private DatePicker promoEndDate;
    @FXML private Label messageLabel;

    private Main mainApp;
    private List<PromotionDTO> allPromotions;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        colPromoId.setCellValueFactory(new PropertyValueFactory<>("promotionId"));
        colPromoName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPromoMultiplier.setCellValueFactory(new PropertyValueFactory<>("bonusMultiplier"));

        loadPromotions();

        searchField.textProperty().addListener((obs, old, newVal) -> filterPromotions(newVal));
    }

    private void loadPromotions() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_PROMOTIONS);
            Response response = socket.sendRequest(request);

            System.out.println("Ответ сервера: " + response.getStatus());
            System.out.println("Данные: " + response.getData());

            if (response.getStatus().equals("SUCCESS")) {
                allPromotions = (List<PromotionDTO>) response.getData();
                System.out.println("Загружено акций: " + allPromotions.size());
                promotionsTable.setItems(FXCollections.observableArrayList(allPromotions));
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (Exception e) {
            messageLabel.setText("Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterPromotions(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            promotionsTable.setItems(FXCollections.observableArrayList(allPromotions));
            return;
        }
        String lowerKeyword = keyword.toLowerCase();
        List<PromotionDTO> filtered = allPromotions.stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
        promotionsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleSearch() {
        filterPromotions(searchField.getText());
    }

    @FXML
    private void handleAddPromotion() {
        String name = promoNameField.getText();
        String multiplierText = promoMultiplierField.getText();
        LocalDate start = promoStartDate.getValue();
        LocalDate end = promoEndDate.getValue();

        if (name.isEmpty() || multiplierText.isEmpty() || start == null || end == null) {
            messageLabel.setText("Заполните все поля");
            return;
        }

        try {
            double multiplier = Double.parseDouble(multiplierText);
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.ADD_PROMOTION, new Object[]{name, multiplier, start, end});
            Response response = socket.sendRequest(request);

            if (response.getStatus().equals("SUCCESS")) {
                messageLabel.setText("Акция добавлена!");
                clearFields();
                loadPromotions();
            } else {
                messageLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Введите корректный множитель");
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditPromotion() {
        PromotionDTO selected = promotionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите акцию для редактирования");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setTitle("Редактирование акции");
        nameDialog.setHeaderText("Редактирование акции ID: " + selected.getPromotionId());
        nameDialog.setContentText("Новое название:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isPresent()) {
            String newName = nameResult.get();

            TextInputDialog multiplierDialog = new TextInputDialog(String.valueOf(selected.getBonusMultiplier()));
            multiplierDialog.setTitle("Редактирование акции");
            multiplierDialog.setHeaderText("Редактирование акции ID: " + selected.getPromotionId());
            multiplierDialog.setContentText("Новый множитель бонусов:");

            Optional<String> multiplierResult = multiplierDialog.showAndWait();
            if (multiplierResult.isPresent()) {
                try {
                    double newMultiplier = Double.parseDouble(multiplierResult.get());
                    ClientSocket socket = ClientSocket.getInstance();
                    Request request = new Request(ActionType.UPDATE_PROMOTION, new Object[]{selected.getPromotionId(), newName, newMultiplier});
                    Response response = socket.sendRequest(request);

                    if (response.getStatus().equals("SUCCESS")) {
                        messageLabel.setText("Акция обновлена!");
                        loadPromotions();
                    } else {
                        messageLabel.setText("Ошибка: " + response.getMessage());
                    }
                } catch (NumberFormatException e) {
                    messageLabel.setText("Введите корректный множитель");
                } catch (Exception e) {
                    messageLabel.setText("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleDeletePromotion() {
        PromotionDTO selected = promotionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите акцию для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить акцию \"" + selected.getName() + "\"?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                ClientSocket socket = ClientSocket.getInstance();
                Request request = new Request(ActionType.DELETE_PROMOTION, selected.getPromotionId());
                Response response = socket.sendRequest(request);

                if (response.getStatus().equals("SUCCESS")) {
                    messageLabel.setText("Акция удалена!");
                    loadPromotions();
                } else {
                    messageLabel.setText("Ошибка: " + response.getMessage());
                }
            } catch (Exception e) {
                messageLabel.setText("Ошибка: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        promoNameField.clear();
        promoMultiplierField.clear();
        promoStartDate.setValue(null);
        promoEndDate.setValue(null);
    }
}