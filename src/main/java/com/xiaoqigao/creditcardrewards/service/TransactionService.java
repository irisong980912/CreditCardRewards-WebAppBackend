package com.xiaoqigao.creditcardrewards.service;

import com.xiaoqigao.creditcardrewards.DAO.TransactionDAO;
import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.exception.TransactionServiceException;
import com.xiaoqigao.creditcardrewards.model.MonthlyReport;
import com.xiaoqigao.creditcardrewards.model.RewardsReport;
import com.xiaoqigao.creditcardrewards.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public RewardsReport getMonthlyRewardsReport(List<Transaction> transactionList) {

        RewardsReport report = new MonthlyReport(transactionList);

        return report;


    }




}
