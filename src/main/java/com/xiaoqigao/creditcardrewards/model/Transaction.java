package com.xiaoqigao.creditcardrewards.model;

import com.xiaoqigao.creditcardrewards.constant.MerchantCode;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Transaction {
    /** primary key */
    int id;

    /** unique key */
    String transactionName;

    /**  separate date for easier retrieval of monthly transactions */
    String postYear;
    String postMonth;
    String postDay;
    String merchantCode;
    int amountCents;

    /**
     * A recursive helper function that helps calculate rewards based on Rule 6 and 7
     * @param amountDollar amountCents / 100
     * @param isSportCheck is the transaction completed at sportcheck
     * @return transaction level points
     */
    private int transLevelPointsHelper(int amountDollar, boolean isSportCheck) {
        int points;

        if ((amountDollar >= 20) && isSportCheck) { // rule 6: 75 points for $20 sportcheck
            points = 75 + transLevelPointsHelper(amountDollar - 20, true);

        } else{
            points = amountDollar;
        }

        return points;
    }

    /**
     * A function that helps calculate rewards based on Rule 6 and 7
     * @return transaction level points
     */
    public int calculateTransLevelPoints() {

        return transLevelPointsHelper(this.amountCents / 100,
                this.merchantCode.equals(MerchantCode.SPORT_CHECK));

    }
}
