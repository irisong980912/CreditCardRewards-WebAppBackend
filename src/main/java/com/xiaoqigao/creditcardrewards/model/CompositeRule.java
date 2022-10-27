package com.xiaoqigao.creditcardrewards.model;

import java.util.List;

public interface CompositeRule {

    /**
     * Calculate total reward point based on the list of transactions given
     * @param transactionList
     * @return
     */
    public int calculatePoint(List<Transaction> transactionList);

}
