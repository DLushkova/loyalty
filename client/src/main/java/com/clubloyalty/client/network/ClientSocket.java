package com.clubloyalty.client.network;

import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected;

    private ClientSocket() {}

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        System.out.println("Подключение к серверу " + host + ":" + port);
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        isConnected = true;
        System.out.println("Подключено к серверу!");
    }

    public Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        System.out.println("Отправка запроса: " + request.getAction());
        out.writeObject(request);
        out.flush();
        Response response = (Response) in.readObject();
        System.out.println("Получен ответ: " + response.getStatus());
        return response;
    }

    public void disconnect() throws IOException {
        isConnected = false;
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
        System.out.println("Отключено от сервера");
    }

    public boolean isConnected() {
        return isConnected;
    }
}