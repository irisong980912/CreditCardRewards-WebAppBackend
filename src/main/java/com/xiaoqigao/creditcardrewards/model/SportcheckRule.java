package com.xiaoqigao.creditcardrewards.model;

public class SportcheckRule implements TransactionLevelRule{


    /**
     * staitc variable reference of single instance of type MonthlyRule
     */
    private static SportcheckRule singleInstance = null;

    /**
     * private constructor;
     */
    private SportcheckRule(){}

    public static SportcheckRule getInstance() {
        if (singleInstance == null) {
            singleInstance = new SportcheckRule();
        }
        return singleInstance;
    }

    /**
     * A function that calculates transaction-level points based on Rule 6 and 7.
     * @return transaction level point
     */
    @Override
    public int calculatePoint(Transaction transaction) {

        int amountDollar = transaction.getAmountCents() / 100;
        int rewards = amountDollar / 20 * 75;
        int rem = amountDollar - (amountDollar / 20) * 20;

        return rewards + rem;

    }

}
