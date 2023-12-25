package ru.itis.crocodile;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WelcomeWindow extends Application {
    private static final String SERVER_IP = "127.0.0.1"; // IP адрес сервера
    private static final int SERVER_PORT = 5000; // Порт сервера
    private PrintWriter writer;
    private BufferedReader reader;
    private VBox welcomeLayout;
    private Socket socket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        welcomeLayout = new VBox(10);
        welcomeLayout.setAlignment(Pos.CENTER);

        // Apply Bootstrap styles to the layout
        welcomeLayout.getStyleClass().addAll("p-4", "bg-light");

        // Create a h1 label for the game title
        Label gameTitle = new Label("Игра Крокодил");
        gameTitle.getStyleClass().add("h1");

        // Spinner to select the number of rounds
        Spinner<Integer> roundsSpinner = new Spinner<>();
        roundsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Field for entering the room code
        TextField joinCodeField = new TextField();
        joinCodeField.setMaxWidth(200);
        joinCodeField.setPromptText("Введите код для подключения");
        joinCodeField.getStyleClass().addAll("form-control");

        // Button to join the room by code
        Button joinButton = new Button("Присоединиться к комнате");
        joinButton.getStyleClass().addAll("btn", "btn-primary");
        joinButton.setOnAction(e -> {
            String joinCode = joinCodeField.getText();
            // Join room logic using the entered code
            joinRoom(joinCode);
        });

        // Button to create a new room
        Button createRoomButton = new Button("Создать новую комнату");
        createRoomButton.getStyleClass().addAll("btn", "btn-success");
        createRoomButton.setOnAction(e -> {
            int numberOfRounds = roundsSpinner.getValue();
            // Logic to create a new room with the selected number of rounds
            createNewRoom(numberOfRounds);
        });

        welcomeLayout.getChildren().addAll(
                gameTitle,
                new Label("Поле ввода кода для присоединения:"),
                joinCodeField,
                joinButton,
                new Label("Выберите количество раундов:"),
                roundsSpinner,
                createRoomButton
        );

        Scene welcomeScene = new Scene(welcomeLayout, 400, 350);
        welcomeScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet()); // Apply BootstrapFX styles
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();

        connectToServer(); // Establish connection to the server
    }

    private void createNewRoom(int numberOfRounds) {
        String roomCode = getCodeFromServer();
        openCrocodileClient(roomCode);
    }

    private String getCodeFromServer() {
        try {
            writer.println("GENERATE_ROOM_CODE"); // Send request to server for room code
            String response = reader.readLine(); // Get room code response from server
            if (response != null && response.startsWith("ROOM_CREATED:")) {
                return response.substring(13); // Return the room code
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void joinRoom(String roomCode) {
        openCrocodileClient(roomCode);
    }

    private void openCrocodileClient(String roomCode) {
        CrocodileClient crocodileClient = new CrocodileClient(roomCode, socket); // Передаем сокет в конструктор CrocodileClient
        Stage stage = new Stage();
        crocodileClient.start(stage);
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT); // Сохраняем сокет
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
        // Обработка сообщений от сервера
    }

    // Важно: завершить работу приложения корректно закрыв сокет при выходе
    @Override
    public void stop() {
        if (writer != null) {
            writer.println("DISCONNECT"); // Отправляем серверу сообщение об отключении
            writer.close();
        }
        Platform.exit();
    }
}
