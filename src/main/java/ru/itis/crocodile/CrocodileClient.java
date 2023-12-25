package ru.itis.crocodile;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrocodileClient extends Application {
    private static final String SERVER_IP = "127.0.0.1"; // IP адрес сервера
    private static final int SERVER_PORT = 5000; // Порт сервера

    private PrintWriter writer;
    private BufferedReader reader;

    private TextArea chatArea;
    private TextField messageInput;
    private Canvas canvas;
    private Timeline timer;
    private Label wordLabel;
    private Label timerLabel;
    private int secondsRemaining;

    private Socket socket; // Добавляем поле для хранения сокета

    public CrocodileClient(String roomCode, Socket socket) {
        this.socket = socket; // Сохраняем переданный сокет
        connectToServer(); // Вызываем метод подключения к серверу
        sendMessage("JOIN_ROOM:" + roomCode); // Отправляем команду на подключение к комнате
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

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

        chatArea.getStyleClass().add("form-control");
        messageInput.getStyleClass().add("form-control");
        sendButton.getStyleClass().setAll("btn", "btn-primary");
        startRoundButton.getStyleClass().setAll("btn", "btn-success");
        endGameButton.getStyleClass().setAll("btn", "btn-danger");

        sendButton.setOnAction(e -> sendMessage());
        startRoundButton.setOnAction(e -> startRound());
        endGameButton.setOnAction(e -> endGame());

        wordLabel = new Label();
        wordLabel.getStyleClass().add("h1");

        timerLabel = new Label();
        timerLabel.getStyleClass().add("h1");

        // Проверка, содержится ли уже wordLabel в chatBox
        if (!chatBox.getChildren().contains(wordLabel)) {
            // Получение списка слов из файла words.txt
            List<String> wordsList = readWordsFromFile("words.txt");

            // Установка случайного слова в лейбл
            setRandomWord(wordLabel, wordsList);

            // Добавление лейбла в интерфейс
            chatBox.getChildren().addAll(chatArea, messageInput, sendButton, wordLabel, timerLabel);
        }

        // Инициализация таймера для обновления слова каждые 60 секунд
        secondsRemaining = 60;
        timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                secondsRemaining--;
                Platform.runLater(() -> updateTimerLabel());

                if (secondsRemaining <= 0) {
                    Platform.runLater(() -> updateWord());
                    secondsRemaining = 60;
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(startRoundButton, endGameButton);

        VBox canvasAndButtons = new VBox(10);
        canvasAndButtons.getChildren().addAll(wordLabel, canvas, buttonsBox);
        canvasAndButtons.setAlignment(Pos.CENTER);

        root.setCenter(canvasAndButtons);
        root.setRight(chatBox);

        Scene scene = new Scene(root, 1000, 500);

        // Apply BootstrapFX style to the scene
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Handle clearing the canvas on "C" key press
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.C) {
                clearCanvas();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Client");
        primaryStage.show();

        connectToServer();
    }

    private void updateTimerLabel() {
        timerLabel.setText("Time left: " + secondsRemaining + "s");
    }

    private void updateWord() {
        List<String> wordsList = readWordsFromFile("words.txt");
        setRandomWord(wordLabel, wordsList);
    }


    private void connectToServer() {
        if (socket != null && socket.isConnected()) {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Thread readerThread = new Thread(() -> {
                    try {
                        while (true) {
                            String message = reader.readLine();
                            if (message != null) {
                                Platform.runLater(() -> processMessage(message));
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
    }

    private void processMessage(String message) {
        if (message.startsWith("DRAW:PRESS:")) {
            drawOnCanvas(message);
        } else if (message.startsWith("DRAW:DRAG:")) {
            drawOnCanvas(message);
        } else if (message.equals("CLEAR_CANVAS")) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            chatArea.appendText(message + "\n"); // Отображение сообщений чата
        }
    }

    private void drawOnCanvas(String message) {
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
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        writer.println("CLEAR_CANVAS");
    }

    private List<String> readWordsFromFile(String filename) {
        List<String> wordsList = new ArrayList<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = br.readLine()) != null) {
                wordsList.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordsList;
    }

    // Метод для установки случайного слова в лейбл
    private void setRandomWord(Label wordLabel, List<String> wordsList) {
        if (!wordsList.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(wordsList.size());
            String randomWord = wordsList.get(randomIndex);
            wordLabel.setText("Word: " + randomWord);
        }
    }

    private void startRound() {
        // Logic for starting a round
        timer.stop();
        secondsRemaining = 60;
        updateWord();
        updateTimerLabel();

        // Запуск таймера снова
        timer.play();
        // Logic for starting a round goes here
        // You can add your implementation for starting a new round
        // This might involve clearing the canvas, resetting the game state, etc.
        // For example, you can call a method to reset the canvas and game state
    }

    private void endGame() {
        // Logic for ending the game
    }
}
