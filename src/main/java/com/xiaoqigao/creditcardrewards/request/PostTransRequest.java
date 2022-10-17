package com.xiaoqigao.creditcardrewards.request;

import lombok.Data;

@Data
public class PostTransRequest {
    private String transaction_name;
    private String date; // 2021-05-09
    private String merchant_code;
    private int amount_cents;

    public String getTransactionName() {
        return transaction_name;
    }

    public String getDate() {
        return date;
    }

    public String getMerchantCode() {
        return merchant_code;
    }

    public int getAmountCents() {
        return amount_cents;
    }
}
