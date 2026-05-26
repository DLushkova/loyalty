package com.clubloyalty.client.util;

import com.clubloyalty.common.dto.UserDTO;

public class SessionManager {
    private static SessionManager instance;
    private UserDTO currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(UserDTO user) {
        this.currentUser = user;
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}