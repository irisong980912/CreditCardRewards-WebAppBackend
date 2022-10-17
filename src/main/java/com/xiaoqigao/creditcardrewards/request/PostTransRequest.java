package com.xiaoqigao.creditcardrewards.request;

import lombok.Data;

/**
 * Request for posting a transaction
 */
@Data
public class PostTransRequest {
    private String transaction_name; // follows example naming convention
    private String date; // ex. 2021-05-09
    private String merchant_code; // follows example naming convention
    private int amount_cents; // follows example naming convention

    /**
     * Getter method for transaction_name
     * @return transaction name
     */
    public String getTransactionName() {
        return transaction_name;
    }

    /**
     * Getter method for date
     * @return transaction name
     */
    public String getDate() {
        return date;
    }

    /**
     * Getter method for merchant code
     * @return merchant code
     */
    public String getMerchantCode() {
        return merchant_code;
    }

    /**
     * Getter method for amount_cents
     * @return amount cents
     */
    public int getAmountCents() {
        return amount_cents;
    }
}
