package com.xiaoqigao.creditcardrewards.service;

import com.xiaoqigao.creditcardrewards.DAO.TransactionDAO;
import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.constant.MerchantCode;
import com.xiaoqigao.creditcardrewards.exception.TransactionServiceException;
import com.xiaoqigao.creditcardrewards.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class TransactionService {

    /** Dependency Injection */
    @Autowired
    TransactionDAO transactionDAO;

    /**
     * Post one transaction.
     * @param transactionName
     * @param dateString
     * @param merchantCode
     * @param amountCents
     */
    public void postOneTransaction(String transactionName, String dateString, String merchantCode, int amountCents) throws Exception {

        // check if the transactionName is unique
        List<Transaction> transactionList = this.transactionDAO.selectByTransactionName(transactionName);
        if (transactionList.size() != 0) {
            throw new TransactionServiceException(Status.TRANSACTION_ALREADY_POSTED);
        }

        // check if the dateString follows the format of 2021-05-01
        boolean checkFormat = dateString.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})");
        if (!checkFormat) {
            throw new TransactionServiceException(Status.WRONG_DATE_STRING_FORMAT);
        }

        // separate the date string
        String dateParts[] = dateString.split("-");

        // get day, month, and year
        String year = dateParts[0];
        String month = dateParts[1];
        String day = dateParts[2];

        // check if the given date is out of range
        if (Integer.valueOf(month) < 0 || Integer.valueOf(month) > 12
                || Integer.valueOf(day) > 30 || Integer.valueOf(day) < 0) {
            throw new TransactionServiceException(Status.WRONG_DATE_STRING_FORMAT);
        }

        if (amountCents < 0) {
            throw new TransactionServiceException(Status.NEGATIVE_AMOUNT_CENTS);
        }

        if (merchantCode == null) {
            throw new TransactionServiceException(Status.MERCHANT_CODE_IS_NULL);
        }

        if (transactionName == null) {
            throw new TransactionServiceException(Status.TRANSACTION_NAME_IS_NULL);
        }

        // build a new transaction
        Transaction newTrans = Transaction.builder().transactionName(transactionName)
                                                    .postYear(year)
                                                    .postMonth(month)
                                                    .postDay(day)
                                                    .merchantCode(merchantCode)
                                                    .amountCents(amountCents)
                                                    .build();

        // insert the record into database
        this.transactionDAO.insert(newTrans);


    }

    /**
     * Get the monthly transaction list from the database based on year and month
     * @param year
     * @param month
     */
    public List<Transaction> getMonthlyTransactionList(String year, String month) throws TransactionServiceException {
        // get a list of transactions by given month
        List<Transaction> transactionList = this.transactionDAO.selectByYearMonth(year, month);

        // check if there is any transactions in the given Year and Month. If null or empty, throw exception
        if (transactionList == null || transactionList.size() == 0) {
            throw new TransactionServiceException(Status.MONTH_NO_TRANSACTIONS);
        }

        return transactionList;
    }

    /**
     * Get the maximum point for the given month
     * @param transactionList a list of monthly transaction
     */
    public int getMonthlyMaxPoint(List<Transaction> transactionList) {

        HashMap<String, Integer> map = aggregateMonthlyTrans(transactionList);

        int spAmt = map.getOrDefault(MerchantCode.SPORT_CHECK, 0); // sportscheck
        int thAmt = map.getOrDefault(MerchantCode.TIM_HORTONS, 0);; // tim_hortons
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
     * A helper function that aggregates the monthly expenditure for each merchant.
     * @param transactions
     * @return a hashmap
     */
    private HashMap<String, Integer> aggregateMonthlyTrans(List<Transaction> transactions) {

        List<String> merchantsForReward = MerchantCode.makeRewardMerchantList(); // gets a list of all the merchants mentioned in the rules

        HashMap<String, Integer> result = new HashMap<>();

        for (Transaction t : transactions) {

            String merchantCode = t.getMerchantCode();

            // handle the other amount
            if (!merchantsForReward.contains(merchantCode)) {
                merchantCode = MerchantCode.OTHER;
            }

            int newAmountCents = result.getOrDefault(merchantCode, 0) + t.getAmountCents();
            result.put(merchantCode, newAmountCents);

        }

        return result;
    }

    /**
     * A greedy helper function for calculating the maximum points earned for the month
     * Rule 3 and Rule 5 are eliminated because they are not as cost-efficient as other rules combined.
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
