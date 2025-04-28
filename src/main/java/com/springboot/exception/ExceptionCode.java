package com.springboot.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "Member not found"),
    LOGOUT_FAILED(404, "Logout failed"),
    USERNAME_NOT_FOUND(404, "Username Not Found"),
    INVALID_MEMBER_STATUS(400, "Invalid member status"),
    UNAUTHORIZED_OPERATION(403, "Can not access this resource"),
    ANSWER_NOT_FOUND(404, "Answer not found"),
    ANSWER_EXISTS(409, "Answer exists"),
    CANNOT_CHANGE_QUESTION(403, "Question already answered"),
    QUESTION_NOT_FOUND(404, "Question not found"),
    NOTICE_NOT_FOUND(404, "Notice not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    REPORT_NOT_FOUND(404, "Report Not found"),
    CANNOT_CANCEL(400, "Cannot cancel log"),
    LOG_TYPE_INVALID(400, "LogType invalid"),
    EXISTING_MEMBER(409, "This MEMBER already exists"),
    FORBIDDEN(403, "Access not allowed"),
    MEMBER_DEACTIVATED(403,"This account deactivated"),
    MEMBER_DELETED(409,"This Member already deleted"),
    CANCEL_MEMBERSHIP(409, "You cannot rejoin within six months of withdrawal."),
    NICKNAME_ALREADY_USED(409, "This nickname already used"),
    UNAUTHORIZED(401, "Unauthorized"),
    SECURITY_EXCEPTION(401, "Security token is invalid or expired"),
    CATEGORY_EXISTS(409, "Category exists"),
    CATEGORY_NOT_FOUND(404, "Category not found"),
    CANNAT_UPDATE_CATEGORY(400, "It's a fixed category"),
    RECORD_NOT_FOUND(404, "Record not found"),
    MONTHLY_REPORT_NOT_FOUND(404, "Monthly report not found"),
    REPORT_EXISTS(409, "Report already exists"),
    GPT_FAILED(500, "Gpt analysis error"),
    NOT_FOUND(404, "Not Found"),
    REPORT_GENERATION_FAILED(500, "GPT analysis error"),
    TOKEN_NOT_EXIST(401, "Token not exist"),
    INVALID_REPORT_TYPE(500, "Invalid Report type"),
    INVALID_SERVER_ERROR(500, "Invalid server error"),
    S3_DELETE_FAILED(500, "S3 delete failed"),
    S3_POST_FAILED(500, "S3 post failed"),
    GOOGLE_CALENDAR_FAILED(500, "Google calender Failed");
    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
