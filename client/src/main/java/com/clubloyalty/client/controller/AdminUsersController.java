package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.dto.UserDTO;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUsersController {

    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Integer> colUserId;
    @FXML private TableColumn<UserDTO, String> colLogin;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, Integer> colRoleId;
    @FXML private TableColumn<UserDTO, Boolean> colBlocked;
    @FXML private TableColumn<UserDTO, String> colStatus;
    @FXML private TextField searchField;

    private Main mainApp;
    private List<UserDTO> allUsers;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));
    }

    private void setupTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRoleId.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        colBlocked.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("loyaltyStatus"));
    }

    private void loadUsers() {
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GET_ALL_USERS);
            Response response = socket.sendRequest(request);
            if (response.getStatus().equals("SUCCESS")) {
                allUsers = (List<UserDTO>) response.getData();
                usersTable.setItems(FXCollections.observableArrayList(allUsers));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            usersTable.setItems(FXCollections.observableArrayList(allUsers));
            return;
        }
        String lowerKeyword = keyword.toLowerCase();
        List<UserDTO> filtered = allUsers.stream()
                .filter(u -> u.getLogin().toLowerCase().contains(lowerKeyword) ||
                        (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
        usersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleSearchUsers() {
        filterUsers(searchField.getText());
    }

    @FXML
    private void handleBlockUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пользователя");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            socket.sendRequest(new Request(ActionType.BLOCK_USER, selected.getUserId()));
            loadUsers();
            showAlert("Пользователь заблокирован");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUnblockUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пользователя");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            socket.sendRequest(new Request(ActionType.UNBLOCK_USER, selected.getUserId()));
            loadUsers();
            showAlert("Пользователь разблокирован");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пользователя");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getFullName());
        dialog.setTitle("Редактирование");
        dialog.setHeaderText("Редактирование ФИО пользователя " + selected.getLogin());
        dialog.setContentText("Новое ФИО:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get();
            try {
                ClientSocket socket = ClientSocket.getInstance();
                socket.sendRequest(new Request(ActionType.UPDATE_USER, new Object[]{selected.getUserId(), newName}));
                loadUsers();
                showAlert("ФИО обновлено");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пользователя");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить пользователя " + selected.getLogin() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ClientSocket socket = ClientSocket.getInstance();
                socket.sendRequest(new Request(ActionType.DELETE_USER, selected.getUserId()));
                loadUsers();
                showAlert("Пользователь удалён");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleChangeStatus() {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пользователя");
            return;
        }

        // Создаем диалог с кастомными кнопками
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Изменение статуса");
        dialog.setHeaderText("Выберите новый статус для " + selected.getLogin());

        // Устанавливаем тип кнопок
        ButtonType confirmButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Создаем выпадающий список
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Новичок", "Постоянный", "VIP");

        // Устанавливаем текущий статус
        String currentStatus = selected.getLoyaltyStatus();
        if (currentStatus != null) {
            if (currentStatus.equals("VIP")) comboBox.setValue("VIP");
            else if (currentStatus.equals("Постоянный")) comboBox.setValue("Постоянный");
            else comboBox.setValue("Новичок");
        } else {
            comboBox.setValue("Новичок");
        }

        dialog.getDialogPane().setContent(comboBox);

        // Конвертируем результат в ID статуса
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                String selectedStatus = comboBox.getValue();
                if ("VIP".equals(selectedStatus)) return 3;
                if ("Постоянный".equals(selectedStatus)) return 2;
                return 1;
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        if (result.isPresent()) {
            int newStatusId = result.get();
            try {
                ClientSocket socket = ClientSocket.getInstance();
                Response response = socket.sendRequest(new Request(ActionType.UPDATE_CLIENT_STATUS,
                        new Object[]{selected.getUserId(), newStatusId}));
                if (response.getStatus().equals("SUCCESS")) {
                    loadUsers();
                    String statusName = newStatusId == 3 ? "VIP" : (newStatusId == 2 ? "Постоянный" : "Новичок");
                    showAlert("Статус пользователя обновлён на " + statusName);
                } else {
                    showAlert("Ошибка: " + response.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Ошибка: " + e.getMessage());
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}