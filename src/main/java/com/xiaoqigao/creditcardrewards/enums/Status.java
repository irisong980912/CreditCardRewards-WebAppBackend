package com.xiaoqigao.creditcardrewards.enums;

/**
 * Customizes request status for exception handling
 */
public enum Status {

    OK(1200, "Successful Request."),
    MONTH_NO_TRANSACTIONS(1001, "No transactions were posted in the given month."),
    WRONG_DATE_STRING_FORMAT(1002, "The date string representation is wrong."),
    TRANSACTION_ALREADY_POSTED(1003, "Transaction with the same name has already been posted."),
    NEGATIVE_AMOUNT_CENTS(1004, "Amount cents cannot be negative."),
    MERCHANT_CODE_IS_NULL(1005, "Merchant code is null."),
    TRANSACTION_NAME_IS_NULL(1006, "Transaction name is null.");

    private int code;
    private String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }


    /**
     * Getter method for code
     * @return status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Getter method for message
     * @return status message
     */
    public String getMessage() {
        return message;
    }
}
