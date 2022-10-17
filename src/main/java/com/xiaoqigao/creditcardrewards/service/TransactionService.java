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

    @Autowired
    TransactionDAO transactionDAO;

    public void deleteAll() {
        this.transactionDAO.deleteAll();
    }
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

        // Getting day, month, and year
        String year = dateParts[0];
        String month = dateParts[1];
        String day = dateParts[2];

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

        Transaction newTrans = Transaction.builder().transactionName(transactionName)
                                                    .postYear(year)
                                                    .postMonth(month)
                                                    .postDay(day)
                                                    .merchantCode(merchantCode)
                                                    .amountCents(amountCents)
                                                    .build();

        // do not avoid duplicated transaction: a customer might purchase the same thing multiple times in a day

        this.transactionDAO.insert(newTrans);


    }

    public List<Transaction> getMonthlyTransactionList(String year, String month) throws TransactionServiceException {
        // get a list of transactions by given month
        List<Transaction> transactionList = this.transactionDAO.selectByYearMonth(year, month);

        // check if there is any transactions in the given Year and Month. If null or empty, throw exception
        if (transactionList == null || transactionList.size() == 0) {
            throw new TransactionServiceException(Status.MONTH_NO_TRANSACTIONS);
        }

        return transactionList;
    }

    // returns the maximum point for the given month
    public int getMonthlyMaxPoint(String year, String month) throws TransactionServiceException {

        // get a list of transactions by given month
        List<Transaction> transactionList = getMonthlyTransactionList(year, month);

        // calculate the maximum monthly award points in the given month
        return maximumMonthlyPoint(transactionList);
    }

    public int getTransactionLevelPointByName(String transactionName) throws TransactionServiceException {

        // get the transaction by transactionName such as T01
        List<Transaction> transactionList = this.transactionDAO.selectByTransactionName(transactionName);
        if (transactionList == null || transactionList.size() == 0) {
            throw new TransactionServiceException(Status.TRANSACTION_NOT_FOUND);
        }

        Transaction transaction = transactionList.get(0);

        boolean isSportCheck = transaction.getMerchantCode().equals(MerchantCode.SPORT_CHECK);

        return transLevelPoints(transaction.getAmountCents() / 100, isSportCheck);
    }

    // ------------- below are helper functions for calculating maximumMonthlyPoint -------------

    // push all the merchants stated in the reward rules to a list

    private HashMap<String, Integer> aggregateMonthlyTrans(List<Transaction> transactions) {

        List<String> merchantsForReward = MerchantCode.makeRewardMerchantList();

        // 1. prepare a list of all the merchantCodes -> dfs seen
        HashMap<String, Integer> result = new HashMap<>();

        for (Transaction t : transactions) {

            String merchantCode = t.getMerchantCode();

            // handle the other amount
            if (!merchantsForReward.contains(merchantCode)) {
                merchantCode = MerchantCode.OTHER;
            }

            int newAmountCents = result.getOrDefault(merchantCode, 0) + t.getAmountCents();
            result.put(merchantCode, newAmountCents);


            System.out.println("-------- aggregateMonthlyTrans --------");
            System.out.println("merchantCode: " + merchantCode + " newAmountCents: " + newAmountCents);
            System.out.println("--------------------");
            System.out.println("");


        }

        return result;
    }

    private int maximumMonthlyPoint(List<Transaction> transactions) {

        HashMap<String, Integer> map = aggregateMonthlyTrans(transactions);

        int spAmt = map.getOrDefault(MerchantCode.SPORT_CHECK, 0); // sports check
        int thAmt = map.getOrDefault(MerchantCode.TIM_HORTONS, 0);; // timHortons
        int subwayAmt = map.getOrDefault(MerchantCode.SUBWAY, 0); // subway
        int otherAmt = map.getOrDefault(MerchantCode.OTHER, 0);

        // get the cents
        int remainingCents = spAmt % 100 + thAmt % 100 + subwayAmt % 100 + otherAmt % 100;

        System.out.println("-------- maixmumMonthlyPoint --------");
        System.out.println("before spAmt: " + spAmt + " thAmt: " + thAmt + " subwayAmt: " + subwayAmt);
        System.out.println("after spAmt: " + spAmt / 100 + " thAmt: " + thAmt / 100 + " subwayAmt: " + subwayAmt / 100);
        System.out.println("demical: " + remainingCents);
        System.out.println("otherAmt: " + otherAmt / 100);
        System.out.println("--------------------");
        System.out.println("");

        // then dp: maximize the point. only need to see the integer
        int rulePoints = maximizeMonthlyRecursive(spAmt / 100, thAmt / 100, subwayAmt / 100);

        System.out.println("resulting points except other and decimal: " + rulePoints);

        return rulePoints + otherAmt / 100 + remainingCents / 100;
    }

    // a greedy algorithm for calculating the maximum points earned for the month
    // rule 3 and 5 are eliminated because they are not as cost-efficient as other combination rules. For more details,
    // please go to readme.md
    private int maximizeMonthlyRecursive(int spAmt, int thAmt, int subwayAmt) {
        // base case: all of them are 0
        if ((spAmt == 0) && (thAmt == 0) && (subwayAmt == 0)) {
            return 0;
        }

        int points;

        if (spAmt >= 75 && thAmt >= 25 && subwayAmt >= 25) { // rule 1: 500 points for sportcheck 75,  timHortons $25, subway $25

            System.out.println("rule 1------------");
            points = 500 + maximizeMonthlyRecursive(spAmt - 75, thAmt - 25, subwayAmt - 25);


        } else if (spAmt >= 75 && thAmt >= 25 && subwayAmt < 25) { // rule 2: 300 points for sportcheck 75,  timHortons $25

            System.out.println("rule 2------------");
            points = 300 + maximizeMonthlyRecursive(spAmt - 75, thAmt - 25, subwayAmt);

        } else if (spAmt >= 25 && thAmt >= 10 && subwayAmt >= 10) { // rule 4: 150 points for sportcheck 25,  timHortons 10, subway 10

            System.out.println("rule 4------------");
            points = 150 + maximizeMonthlyRecursive(spAmt - 25, thAmt - 10, subwayAmt - 10);

        } else if (spAmt >= 20) { // rule 6: 75 points for sportcheck 20

            System.out.println("rule 6------------");
            points = 75 + maximizeMonthlyRecursive(spAmt - 20, thAmt, subwayAmt);

        } else { // rule 7: 1 point for the remaining. base case

            System.out.println("rule 7------------");
            points = spAmt + thAmt + subwayAmt;

        }

        System.out.println("spAmt: " + spAmt + " thAmt: " + thAmt + " subwayAmt: " + subwayAmt);
        System.out.println("points: " + points);
        System.out.println(" ");
        return points;

    }

    // ------------- below are helper functions for calculating individualTransPoints -------------
    private int transLevelPoints(int amountDollar, boolean isSportCheck) {
        // base case: all of them are 0

        int points = 0;

        if ((amountDollar >= 20) && isSportCheck) { // rule 6: 75 points for sportcheck 20

            System.out.println("individualTransPoints rule 6------------" + amountDollar);
            points = 75 + transLevelPoints(amountDollar - 20, true);

        } else{

            points = amountDollar;

        }

        return points;

    }







}
