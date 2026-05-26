package com.clubloyalty.common.network;

import java.io.Serializable;

/**
 * Запрос от клиента к серверу
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private ActionType action;
    private Object data;
    private String token;

    public Request(ActionType action) {
        this.action = action;
    }

    public Request(ActionType action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Request(ActionType action, Object data, String token) {
        this.action = action;
        this.data = data;
        this.token = token;
    }

    // Геттеры и сеттеры
    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}