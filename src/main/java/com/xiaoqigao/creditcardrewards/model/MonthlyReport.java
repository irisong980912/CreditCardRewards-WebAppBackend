package com.xiaoqigao.creditcardrewards.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MonthlyReport extends RewardsReport {

    /** total maximum rewards point for the requested month */
    private int monthlyRewardsPoint;

    /** maximum points for each individual transaction in the month */
    private List<TransactionLevelPoint> transactionPointsList;

    private CompositeRule rule;

    public MonthlyReport(List<Transaction> transactionList) {
        this.makeReport(transactionList);
    }

    /**
     * Makes monthly report based on a list of transactions
     * @param transactionList
     * @return monthly report which has the total rewards point as well as the transaction-level points
     */
    @Override
    public void makeReport(List<Transaction> transactionList) {

        this.rule = MonthlyRule.getInstance();

        int maxMonthlyPoint = rule.calculatePoint(transactionList);

        List<TransactionLevelPoint> levelPointList = new ArrayList<>();
        for (Transaction transaction : transactionList) { // iterate and calculate points for each transaction
            TransactionLevelPoint transactionLevelPoint = transaction.getTransactionLevelPoint();
            levelPointList.add(transactionLevelPoint);
        }

        this.monthlyRewardsPoint = maxMonthlyPoint;
        this.transactionPointsList = levelPointList;

    }

    @Override
    public String toString() {
        return "Total Points: " + monthlyRewardsPoint + "\n" +
                "Transaction Level Points: \n" + transactionPointsList;
    }
}
