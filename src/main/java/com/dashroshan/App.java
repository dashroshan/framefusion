package com.dashroshan;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Load CSS stylesheet
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());

        // Set app properties and run
        Image favicon = new Image(App.class.getResource("icon.png").toExternalForm());
        stage.getIcons().add(favicon);
        stage.setTitle("FrameFusion");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
