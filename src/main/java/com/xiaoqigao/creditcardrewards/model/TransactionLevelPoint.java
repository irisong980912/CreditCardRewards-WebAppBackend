package com.xiaoqigao.creditcardrewards.model;

public class TransactionLevelPoint {

    private String transactionName;
    private int point;
    public TransactionLevelPoint(String transactionName, int point) {
        this.transactionName = transactionName;
        this.point = point;
    }

    @Override
    public String toString() {
        return "\n{" +
                "transaction='" + transactionName + '\'' +
                ", point=" + point +
                "}";
    }
}
