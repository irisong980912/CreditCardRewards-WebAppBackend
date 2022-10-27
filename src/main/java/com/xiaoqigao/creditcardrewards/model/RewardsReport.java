package com.xiaoqigao.creditcardrewards.model;

import lombok.Data;

import java.util.List;

@Data
public abstract class RewardsReport {
    /** total maximum rewards point for the requested month */
    private int monthlyRewardsPoint;

    /** maximum points for each individual transaction in the month */
    private List<TransactionLevelPoint> transactionPointsList;

    private CompositeRule rule;


    protected abstract void makeReport(List<Transaction> transactionList);

}
