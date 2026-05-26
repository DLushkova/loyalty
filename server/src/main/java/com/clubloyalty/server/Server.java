package com.clubloyalty.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Многопоточный сервер
 */
public class Server {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Сервер запущен на порту " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключился: " + clientSocket.getInetAddress());

                // Создаём новый поток для каждого клиента
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}