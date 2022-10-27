package com.xiaoqigao.creditcardrewards.model;

public interface TransactionLevelRule {

    /**
     * Calculate total reward point based on the list of transactions given
     * @param transaction
     * @return
     */
    public int calculatePoint(Transaction transaction);
}
