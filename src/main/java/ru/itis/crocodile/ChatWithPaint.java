package ru.itis.crocodile;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatWithPaint extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a canvas
        Canvas canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2); // Set the line width

        canvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        canvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        canvas.setFocusTraversable(true); // Enable keyboard events on the canvas

        // Chat components
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Type your message...");
        messageInput.setWrapText(true);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String message = messageInput.getText();
            chatArea.appendText("You: " + message + "\n");
            messageInput.clear();
        });

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.getChildren().addAll(chatArea, messageInput, sendButton);

        // Add canvas and chat panel to a BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setRight(chatBox);

        Scene scene = new Scene(root, 800, 400);

        // Handle clearing the canvas on "C" key press
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.C) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });

        primaryStage.setTitle("Chat with Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
