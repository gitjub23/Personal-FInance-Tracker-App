package com.example.financeapp;

import com.example.financeapp.navigation.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Register the primary stage with the SceneManager
        SceneManager.setStage(stage);

        // Load the first screen (Login page)
        SceneManager.switchTo("LoginView");

        stage.setTitle("Finance App");

        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(820);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}