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
            // Carica la vista di login dal file FXML
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/org/univr/telemedicina/gui/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Imposta dimensioni iniziali

            primaryStage.setTitle("TeleMedicina - Login");
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