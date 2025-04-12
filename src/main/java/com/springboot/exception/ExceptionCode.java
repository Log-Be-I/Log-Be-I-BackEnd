package com.springboot.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "Member not found"),
    MEMBER_EXISTS(409, "Member exists"),
    LOGOUT_FAILED(404, "Logout failed"),
    USERNAME_NOT_FOUND(404, "Username Not Found"),
    INVALID_MEMBER_STATUS(400, "Invalid member status"),
    UNAUTHORIZED_OPERATION(403, "Can not access this resource"),
    ANSWER_NOT_FOUND(404, "Answer not found"),
    ANSWER_EXISTS(409, "Answer exists"),
    CANNOT_CHANGE_QUESTION(403, "Question already answered"),
    QUESTION_NOT_FOUND(404, "Question not found"),
    ITEM_NOT_EXIST(404, "Item not found"),
    NOTICE_NOT_EXIST(404, "Notice not found"),
    THEME_NOT_FOUND(404, "Theme not found"),
    ITEM_NOT_FOUND(404, "Item not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    REPORT_NOT_FOUND(404, "Report Not found"),
    EVENT_NOT_FOUND(404, "Event not found"),
    CHATROOM_NOT_FOUND(404, "Chatroom not found"),
    DICE_LOG_NOT_FOUND(404, "DiceLog not found"),
    NOT_ENOUGH_DICE(400, "Dice not Enough"),
    CANNOT_CANCEL(400, "Cannot cancel log"),
    LOG_TYPE_INVALID(400, "LogType invalid"),
    CHAT_NOT_FOUND(404, "Chat not found"),
    UNAUTHORIZED(401, "Unauthorized");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
