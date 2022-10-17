package com.xiaoqigao.creditcardrewards.response;

import lombok.Value;

/**
 * A response that represents maximum point for each transaction
 */
@Value
public class TransactionLevelPointResponse {

    private String transaction_name;
    private int point;
    public TransactionLevelPointResponse(String transactionName, int point) {
        this.transaction_name = transactionName;
        this.point = point;
    }

}
