package com.xiaoqigao.creditcardrewards.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A constant class only carries class-specific variables and methods.
 * There is no object-specific state.
 *
 * Singleton can be passed as a variable to other methods
 * make it a singleton class instead of a static class to benefit from polymorphism
 */
public class MonthlyRule implements CompositeRule {

    /**
     * staitc variable reference of single instance of type MonthlyRule
     */
    private static MonthlyRule singleInstance = null;

    /**
     * private constructor;
     */
    private MonthlyRule(){}

    public static MonthlyRule getInstance() {
        if (singleInstance == null) {
            singleInstance = new MonthlyRule();
        }
        return singleInstance;
    }

    /**
     * A function for calculating the maximum points earned for the month
     *
     * @param transactionList
     * @return maximum monthly reward point
     */
    @Override
    public int calculatePoint(List<Transaction> transactionList) {

        HashMap<String, Integer> map = aggregateMonthlyTrans(transactionList); // aggregates all amounts for distinct merchants together

        int spAmt = map.getOrDefault(MerchantCode.SPORT_CHECK, 0); // sports check
        int thAmt = map.getOrDefault(MerchantCode.TIM_HORTONS, 0);; // timHortons
        int subwayAmt = map.getOrDefault(MerchantCode.SUBWAY, 0); // subway
        int otherAmt = map.getOrDefault(MerchantCode.OTHER, 0);

        // get the cents
        int remainingCents = spAmt % 100 + thAmt % 100 + subwayAmt % 100 + otherAmt % 100;

        // get the points earned based on rules
        int rulePoints = maximizeMonthlyRecursive(spAmt / 100, thAmt / 100, subwayAmt / 100);

        // add rule points, other points, and remaining cents together
        return rulePoints + otherAmt / 100 + remainingCents / 100;
    }

    /** ================================== start of helper ================================== */

    /**
     * Makes a list of merchants that are mentioned in the reward rules.
     * @return a list of merchants
     */
    private List<String> makeRewardMerchantList() {
        List<String> merchantsForReward = new ArrayList<>();
        merchantsForReward.add(MerchantCode.SPORT_CHECK);
        merchantsForReward.add(MerchantCode.TIM_HORTONS);
        merchantsForReward.add(MerchantCode.SUBWAY);

        return merchantsForReward;
    }

    /**
     * A helper function that aggregates the monthly expenditure for each merchant.
     * @param transactions
     * @return a hashmap
     */
    private HashMap<String, Integer> aggregateMonthlyTrans(List<Transaction> transactions) {

        List<String> merchantsForReward = makeRewardMerchantList(); // gets a list of all the merchants mentioned in the rules

        HashMap<String, Integer> result = new HashMap<>();

        for (Transaction t : transactions) {

            String merchantCode = t.getMerchantCode();

            if (!merchantsForReward.contains(merchantCode)) { // if not in the list, then count towards "other" to apply CompositeRule 7
                merchantCode = MerchantCode.OTHER;
            }

            int newAmountCents = result.getOrDefault(merchantCode, 0) + t.getAmountCents();
            result.put(merchantCode, newAmountCents);
        }

        return result;
    }

    /**
     * A greedy helper function for calculating the maximum points earned for the month
     * CompositeRule 3 and CompositeRule 5 are ignored because they are not as cost-efficient as other rules combined.
     * For reasons of elimination, please refer to README.md
     *
     * @param spAmt dollar amount left for sportcheck
     * @param thAmt dollar amount left for tim-hortons
     * @param subwayAmt dollar amount left for subway
     * @return maximum monthly reward point
     */
    private int maximizeMonthlyRecursive(int spAmt, int thAmt, int subwayAmt) {
        // base case: all of them are 0
        if ((spAmt == 0) && (thAmt == 0) && (subwayAmt == 0)) {
            return 0;
        }

        int points;

        if (spAmt >= 75 && thAmt >= 25 && subwayAmt >= 25) { // rule 1: 500 points for sportcheck $75,  timHortons $25, subway $25

            points = 500 + maximizeMonthlyRecursive(spAmt - 75, thAmt - 25, subwayAmt - 25);

        } else if (spAmt >= 75 && thAmt >= 25 && subwayAmt < 25) { // rule 2: 300 points for sportcheck $75,  timHortons $25

            points = 300 + maximizeMonthlyRecursive(spAmt - 75, thAmt - 25, subwayAmt);

        } else if (spAmt >= 25 && thAmt >= 10 && subwayAmt >= 10) { // rule 4: 150 points for sportcheck $25,  timHortons $10, subway $10

            points = 150 + maximizeMonthlyRecursive(spAmt - 25, thAmt - 10, subwayAmt - 10);

        } else if (spAmt >= 20) { // rule 6: 75 points for sportcheck $20

            points = 75 + maximizeMonthlyRecursive(spAmt - 20, thAmt, subwayAmt);

        } else { // rule 7: 1 point for the remaining.

            points = spAmt + thAmt + subwayAmt;

        }

        return points;
    }


    /** ================================== end of helper ================================== */
}
