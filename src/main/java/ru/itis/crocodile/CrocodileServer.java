package ru.itis.crocodile;

import java.io.*;
import java.net.*;
import java.util.*;

public class CrocodileServer {
    private static final int PORT = 5000;
    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;

        public Handler(Socket socket) {
            this.socket = socket;
        }

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

                    if (message.startsWith("DRAW:")) {
                        broadcast(message, writer); // Перенаправить рисование всем клиентам, кроме отправителя
                    } else if (message.equals("CLEAR_CANVAS")) {
                        clearCanvasForAll(); // Очистка холста для всех клиентов
                    } else {
                        broadcast(message, null); // Если это не сообщение о рисовании, передать всем
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
            // Очистка холста для всех клиентов
        }
    }
}
