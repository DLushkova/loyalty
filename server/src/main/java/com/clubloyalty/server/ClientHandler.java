package com.clubloyalty.server;

import com.clubloyalty.common.network.ActionType;
import com.clubloyalty.common.network.Request;
import com.clubloyalty.common.network.Response;
import com.clubloyalty.server.service.AuthService;
import com.clubloyalty.server.service.LoyaltyService;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final AuthService authService;
    private final LoyaltyService loyaltyService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.authService = new AuthService();
        this.loyaltyService = new LoyaltyService();
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = (Request) in.readObject();
                Response response = handleRequest(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Клиент отключился");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private Response handleRequest(Request request) {
        ActionType action = request.getAction();

        switch (action) {
            // ========== АУТЕНТИФИКАЦИЯ ==========
            case LOGIN:
                String[] loginData = (String[]) request.getData();
                return authService.login(loginData[0], loginData[1]);

            case REGISTER:
                Object[] regData = (Object[]) request.getData();
                return authService.register(
                        (String) regData[0], (String) regData[1],
                        (String) regData[2], (String) regData[3], (String) regData[4]
                );

            // ========== КЛИЕНТСКИЕ ОПЕРАЦИИ ==========
            case GET_BONUS_BALANCE:
                return loyaltyService.getBonusBalance((int) request.getData());

            case GET_MONEY_BALANCE:
                return loyaltyService.getMoneyBalance((int) request.getData());

            case GET_ACTIVE_SESSION:
                return loyaltyService.getActiveSession((int) request.getData());

            case GET_MY_SESSIONS:
                return loyaltyService.getMySessions((int) request.getData());

            case GET_TARIFFS:
                return loyaltyService.getTariffs();

            case GET_PROMOTIONS:
                return loyaltyService.getPromotions();

            case GET_CLIENT_STATUS:
                return loyaltyService.getClientStatus((int) request.getData());

            case START_SESSION:
                Object[] startData = (Object[]) request.getData();
                return loyaltyService.startSession(
                        (int) startData[0],
                        (int) startData[1],
                        (int) startData[2],
                        (boolean) startData[3]
                );

            case END_SESSION:
                return loyaltyService.endSession((int) request.getData());

            case REDEEM_BONUS:
                Object[] redeemData = (Object[]) request.getData();
                return loyaltyService.redeemBonus((int) redeemData[0], (double) redeemData[1]);

            // ========== АДМИНИСТРАТИВНЫЕ ОПЕРАЦИИ (ПОЛЬЗОВАТЕЛИ) ==========
            case GET_ALL_USERS:
                return loyaltyService.getAllUsers();

            case BLOCK_USER:
                return loyaltyService.blockUser((int) request.getData());

            case UNBLOCK_USER:
                return loyaltyService.unblockUser((int) request.getData());

            case UPDATE_USER:
                Object[] updateUserData = (Object[]) request.getData();
                return loyaltyService.updateUser((int) updateUserData[0], (String) updateUserData[1]);

            case DELETE_USER:
                return loyaltyService.deleteUser((int) request.getData());

            case UPDATE_CLIENT_STATUS:
                Object[] updateStatusData = (Object[]) request.getData();
                return loyaltyService.updateClientStatus((int) updateStatusData[0], (int) updateStatusData[1]);

            // ========== АДМИНИСТРАТИВНЫЕ ОПЕРАЦИИ (ТАРИФЫ) ==========
            case ADD_TARIFF:
                Object[] tariffData = (Object[]) request.getData();
                return loyaltyService.addTariff(
                        (String) tariffData[0],
                        (double) tariffData[1],
                        (double) tariffData[2]
                );

            case UPDATE_TARIFF:
                Object[] updateTariffData = (Object[]) request.getData();
                return loyaltyService.updateTariff(
                        (int) updateTariffData[0],
                        (String) updateTariffData[1],
                        (double) updateTariffData[2],
                        (double) updateTariffData[3]
                );

            case DELETE_TARIFF:
                return loyaltyService.deleteTariff((int) request.getData());

            // ========== АДМИНИСТРАТИВНЫЕ ОПЕРАЦИИ (АКЦИИ) ==========
            case ADD_PROMOTION:
                Object[] promoData = (Object[]) request.getData();
                return loyaltyService.addPromotion(
                        (String) promoData[0],
                        (double) promoData[1],
                        (LocalDate) promoData[2],
                        (LocalDate) promoData[3]
                );

            case UPDATE_PROMOTION:
                Object[] updatePromoData = (Object[]) request.getData();
                return loyaltyService.updatePromotion(
                        (int) updatePromoData[0],
                        (String) updatePromoData[1],
                        (double) updatePromoData[2]
                );

            case DELETE_PROMOTION:
                return loyaltyService.deletePromotion((int) request.getData());

            // ========== БОНУСЫ И ДЕНЬГИ ==========
            case ADD_BONUS:
                Object[] bonusData = (Object[]) request.getData();
                return loyaltyService.addBonus((int) bonusData[0], (double) bonusData[1]);

            case ADD_MONEY:
                Object[] moneyData = (Object[]) request.getData();
                return loyaltyService.addMoney((int) moneyData[0], (double) moneyData[1]);

            // ========== ОТЧЁТЫ ==========
            case GENERATE_REPORT:
                Object[] reportData = (Object[]) request.getData();
                return loyaltyService.generateReport((LocalDate) reportData[0], (LocalDate) reportData[1]);

            default:
                return new Response("ERROR", "Неизвестная команда: " + action);
        }
    }
}