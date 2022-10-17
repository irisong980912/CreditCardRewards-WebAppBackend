package com.xiaoqigao.creditcardrewards.response;

import com.xiaoqigao.creditcardrewards.enums.Status;
import lombok.Value;

@Value
public class TransactionLevelPointResponse{

    private String transaction_name;
    private int point;
    public TransactionLevelPointResponse(String transactionName, int point) {
        this.transaction_name = transactionName;
        this.point = point;
    }

}
