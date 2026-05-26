package com.clubloyalty.server.factory;

import com.clubloyalty.server.interfaces.ServiceInterface;
import com.clubloyalty.server.service.AuthService;
import com.clubloyalty.server.service.LoyaltyService;

public class ServiceFactory {

    public static ServiceInterface getService(String type) {
        if (type == null) return null;

        switch (type) {
            case "AUTH":
                return new AuthService();
            case "LOYALTY":
                return new LoyaltyService();
            default:
                return null;
        }
    }
}