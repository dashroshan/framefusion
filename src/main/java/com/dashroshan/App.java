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
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);
        stage.setTitle("FrameFusion");
        stage.setScene(scene);
        stage.setResizable(false);
        Image favicon = new Image(App.class.getResource("icon.png").toExternalForm());
        stage.getIcons().add(favicon);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
