package org.univr.telemedicina.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // carica la vista di login dal file fxml
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/org/univr/telemedicina/gui/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1440, 1024); // imposta dimensioni iniziali

            primaryStage.setTitle("telemedicina - login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}