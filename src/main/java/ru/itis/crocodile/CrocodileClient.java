package ru.itis.crocodile;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.*;
import java.net.Socket;

public class CrocodileClient extends Application {
    private static final String SERVER_IP = "127.0.0.1"; // IP адрес сервера
    private static final int SERVER_PORT = 5000; // Порт сервера

    private PrintWriter writer;
    private BufferedReader reader;

    private TextArea chatArea;
    private TextField messageInput;
    private Canvas canvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a canvas
        canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2); // Set the line width

        canvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            sendMessage("DRAW:PRESS:" + e.getX() + ":" + e.getY());
        });

        canvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            sendMessage("DRAW:DRAG:" + e.getX() + ":" + e.getY());
        });



        canvas.setFocusTraversable(true); // Enable keyboard events on the canvas

        BorderPane root = new BorderPane();
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));

        chatArea = new TextArea();
        chatArea.setPrefHeight(350);
        chatArea.setEditable(false);

        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");

        Button sendButton = new Button("Send");
        Button startRoundButton = new Button("Start Round");
        Button endGameButton = new Button("End Game");

        // Apply BootstrapFX styles to components
        chatArea.getStyleClass().add("form-control");
        messageInput.getStyleClass().add("form-control");
        sendButton.getStyleClass().setAll("btn", "btn-primary");
        startRoundButton.getStyleClass().setAll("btn", "btn-success");
        endGameButton.getStyleClass().setAll("btn", "btn-danger");

        sendButton.setOnAction(e -> sendMessage());
        startRoundButton.setOnAction(e -> startRound());
        endGameButton.setOnAction(e -> endGame());


        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(startRoundButton, endGameButton);

        VBox canvasAndButtons = new VBox(10);
        canvasAndButtons.getChildren().addAll(canvas, buttonsBox);
        chatBox.getChildren().addAll(chatArea, messageInput, sendButton);
        root.setCenter(canvasAndButtons);
        root.setRight(chatBox);

        Scene scene = new Scene(root, 1000, 450);

        // Apply BootstrapFX style to the scene
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Handle clearing the canvas on "C" key press
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.C) {
                clearCanvas(); // Очистка холста и отправка сообщения о чистке
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Client");
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            Thread readerThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = reader.readLine();
                        if (message != null) {
                            Platform.runLater(() -> processMessage(message)); // Обработка полученного сообщения
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processMessage(String message) {
        if (message.startsWith("DRAW:PRESS:")) {
            drawOnCanvas(message);
        } else if (message.startsWith("DRAW:DRAG:")) {
            drawOnCanvas(message);
        } else {
            chatArea.appendText(message + "\n"); // Отображение сообщений чата
        }
    }

    private void drawOnCanvas(String message) {
        // Разбор сообщения для получения координат рисования
        String[] parts = message.split(":");
        if (parts.length == 4) {
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);

            GraphicsContext gc = canvas.getGraphicsContext2D();
            if (parts[1].equals("PRESS")) {
                gc.beginPath();
                gc.lineTo(x, y);
                gc.stroke();
            } else if (parts[1].equals("DRAG")) {
                gc.lineTo(x, y);
                gc.stroke();
            }
        }
    }
    private void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            messageInput.clear();
        }
    }
    private void sendMessage(String message) {
        writer.println(message);
    }
    public GraphicsContext getCanvasGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    private void clearCanvas() {
        writer.println("CLEAR_CANVAS");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void startRound() {
        // Logic for starting a round goes here
        // You can add your implementation for starting a new round
        // This might involve clearing the canvas, resetting the game state, etc.
        // For example, you can call a method to reset the canvas and game state
    }

    private void endGame() {
        // Logic for ending the game goes here
        // You can add your implementation for ending the game
        // This might involve closing the connections, displaying scores, etc.
        // For example, you can call a method to close the connections and clean up
    }
}
