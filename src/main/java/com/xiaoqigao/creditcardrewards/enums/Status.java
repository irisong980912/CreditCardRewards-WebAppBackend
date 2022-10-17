package com.xiaoqigao.creditcardrewards.enums;

public enum Status {

    OK(1200, "Successful Request."),
    MONTH_NO_TRANSACTIONS(1001, "No transactions were posted in the given month."),
    TRANSACTION_NOT_FOUND(1002, "Cannot find transaction by the given id."),
    WRONG_DATE_STRING_FORMAT(1003, "The date string representation is wrong."),
    TRANSACTION_ALREADY_POSTED(1004, "Transaction with the same name has already been posted."),
    NEGATIVE_AMOUNT_CENTS(1005, "Amount cents cannot be negative."),
    MERCHANT_CODE_IS_NULL(1006, "Merchant code is null."),
    TRANSACTION_NAME_IS_NULL(1007, "Transaction name is null.");

    private int code;
    private String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
