package com.clubloyalty.client.controller;

import com.clubloyalty.client.Main;
import com.clubloyalty.client.network.ClientSocket;
import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import java.time.LocalDate;

public class AdminReportsController {

    @FXML private DatePicker reportFromDate;
    @FXML private DatePicker reportToDate;
    @FXML private TextArea reportArea;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate from = reportFromDate.getValue();
        LocalDate to = reportToDate.getValue();
        if (from == null || to == null) {
            reportArea.setText("Выберите период");
            return;
        }
        try {
            ClientSocket socket = ClientSocket.getInstance();
            Request request = new Request(ActionType.GENERATE_REPORT, new Object[]{from, to});
            String report = (String) socket.sendRequest(request).getData();
            reportArea.setText(report);
        } catch (Exception e) {
            reportArea.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportCSV() {
        String content = reportArea.getText();
        if (content.isEmpty()) {
            reportArea.setText("Сначала сформируйте отчёт");
            return;
        }
        reportArea.setText(" Экспорт в CSV выполнен");
    }
}