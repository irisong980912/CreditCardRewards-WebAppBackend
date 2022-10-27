package com.xiaoqigao.creditcardrewards.model;

public class OtherRule implements TransactionLevelRule{

    /**
     * staitc variable reference of single instance of type MonthlyRule
     */
    private static OtherRule singleInstance = null;

    /**
     * private constructor;
     */
    private OtherRule(){}

    public static OtherRule getInstance() {
        if (singleInstance == null) {
            singleInstance = new OtherRule();
        }
        return singleInstance;
    }
    /**
     * A function that calculates transaction-level points based on Rule 7.
     * @return transaction level point
     */
    @Override
    public int calculatePoint(Transaction transaction) {

        return transaction.getAmountCents() / 100;

    }

}
