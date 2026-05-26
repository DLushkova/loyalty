package com.clubloyalty.common.network;

public enum ActionType {
    // Аутентификация
    LOGIN,
    REGISTER,

    // Клиентские операции
    GET_BONUS_BALANCE,
    GET_MY_SESSIONS,
    GET_TARIFFS,
    START_SESSION,
    END_SESSION,
    REDEEM_BONUS,
    UPDATE_PROFILE,

    // Административные операции (пользователи)
    GET_ALL_USERS,
    GET_USER_BY_ID,
    BLOCK_USER,
    UNBLOCK_USER,
    UPDATE_USER,
    DELETE_USER,

    // Административные операции (тарифы)
    ADD_TARIFF,
    UPDATE_TARIFF,
    DELETE_TARIFF,
    GET_MONEY_BALANCE,
    GET_ACTIVE_SESSION,

    // Административные операции (акции)
    GET_PROMOTIONS,
    ADD_PROMOTION,
    UPDATE_PROMOTION,
    DELETE_PROMOTION,
    ADD_MONEY,
    GET_CLIENT_STATUS,
    UPDATE_CLIENT_STATUS,
    // Административные операции (бонусы)
    ADD_BONUS,

    // Отчёты
    GENERATE_REPORT

}