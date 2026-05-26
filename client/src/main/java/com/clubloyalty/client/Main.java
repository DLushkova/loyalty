package com.clubloyalty.client;

import com.clubloyalty.client.controller.AdminMainController;
import com.clubloyalty.client.controller.LoginController;
import com.clubloyalty.client.controller.MainController;
import com.clubloyalty.client.controller.RegisterController;
import com.clubloyalty.client.controller.SessionController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showLoginWindow();
    }

    public void showLoginWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setTitle("Вход");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
    }

    public void showMainWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setTitle("Главная");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }

    public void showAdminMainWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminMain.fxml"));
        Parent root = loader.load();

        AdminMainController controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setTitle("Админ панель");
        primaryStage.setScene(new Scene(root, 1200, 750));
        primaryStage.show();
    }

    public void showRegisterWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
        Parent root = loader.load();

        RegisterController controller = loader.getController();
        controller.setMainApp(this);

        Stage stage = new Stage();
        stage.setTitle("Регистрация");
        stage.setScene(new Scene(root, 450, 600));
        stage.show();
    }

    public void showSessionWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Session.fxml"));
        Parent root = loader.load();

        SessionController controller = loader.getController();
        controller.setMainApp(this);

        Stage stage = new Stage();
        stage.setTitle("Начать игру");
        stage.setScene(new Scene(root, 650, 550));
        stage.show();
    }

    public void refreshMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setMainApp(this);
            controller.initialize(); // Принудительная инициализация
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}