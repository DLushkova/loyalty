package com.clubloyalty.common.network;

import java.io.Serializable;

/**
 * Ответ от сервера клиенту
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;   // "SUCCESS" или "ERROR"
    private String message;   // Сообщение для пользователя
    private Object data;      // Данные (UserDTO, список сессий и т.д.)

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Геттеры и сеттеры
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}