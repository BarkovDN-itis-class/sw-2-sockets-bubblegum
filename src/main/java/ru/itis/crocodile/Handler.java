package ru.itis.crocodile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Handler extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private static final Set<PrintWriter> writers = new HashSet<>();

    public Handler(Socket socket) {
        this.socket = socket;
    }
    private static final Map<String, Room> rooms = new HashMap<>();

    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            writers.add(writer);

            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    return;
                }
                System.out.println("Received: " + message);

                if (message.startsWith("GENERATE_ROOM_CODE:")) {
                    handleRoomCreation(message);
                } else if (message.startsWith("JOIN_ROOM:")) {
                    handleRoomJoining(message, writer);
                } else if (message.startsWith("DRAW:")) {
                    broadcast(message, writer);
                } else {
                    broadcast(message, null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writers.remove(writer);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String message, PrintWriter sender) {
        for (PrintWriter writer : writers) {
            if (writer != sender) {
                writer.println(message);
            }
        }
    }

    private void clearCanvasForAll() {
        for (PrintWriter writer : writers) {
            writer.println("CLEAR_CANVAS");
        }
    }

    private void handleRoomCreation(String message) {
        // Получение параметров из сообщения (например, количество раундов)
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            int numberOfRounds = Integer.parseInt(parts[1]);
            // Создание новой комнаты и генерация уникального кода
            String roomCode = generateUniqueRoomCode();
            Room newRoom = new Room(numberOfRounds);
            rooms.put(roomCode, newRoom);
            // Отправка кода комнаты клиенту
            writer.println("ROOM_CREATED:" + roomCode);
        }
    }

    private String generateUniqueRoomCode() {
        // Generate a random 4-digit room code
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10)); // Appending a random digit (0-9)
        }
        System.out.println(code);
        return code.toString();
    }

    private void handleRoomJoining(String message, PrintWriter sender) {
        // Получение параметров из сообщения (например, код комнаты)
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String roomCode = parts[1];
            // Проверка наличия комнаты по указанному коду
            Room roomToJoin = rooms.get(roomCode);
            if (roomToJoin != null) {
                // Добавление клиента в комнату или выполнение других действий по присоединению
                // Например: roomToJoin.addPlayer(sender);
                writer.println("JOINED_ROOM:" + roomCode);
            } else {
                writer.println("ROOM_NOT_FOUND");
            }
        }
    }
}
