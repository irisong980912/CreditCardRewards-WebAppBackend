package com.xiaoqigao.creditcardrewards.service;

import com.xiaoqigao.creditcardrewards.DAO.TestTransactionDAO;
import com.xiaoqigao.creditcardrewards.DAO.TransactionDAO;
import com.xiaoqigao.creditcardrewards.constant.MerchantCode;
import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.exception.TransactionServiceException;
import com.xiaoqigao.creditcardrewards.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for TransactionService
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionDAO transactionDAO;

    @InjectMocks
    private TransactionService transactionService;

    /** constants */
    private static final String TRANSACTION_NAME = "T01";
    private static final String DATE = "2021-05-01";
    private static final String POST_YEAR = "2021";
    private static final String POST_MONTH = "05";
    private static final String POST_DAY = "01";
    private static final String MERCHANT_CODE = "sportscheck";
    private static final int  AMOUNT_CENTS = 1000;

    private static final String WRONG_DATE = "2021/05/01";
    private static final String OUT_RANGE_DATE = "2021-05-58";
    private static final int NEGATIVE_AMOUNT_CENTS = -1000;

    /**
     * Success: Test for the behavious of successfully posting a list of transactions
     */
    @Test
    public void testPostTrans_happyCase() throws Exception {

        when(this.transactionDAO.selectByTransactionName(TRANSACTION_NAME)).thenReturn(List.of());

        this.transactionService.postOneTransaction(TRANSACTION_NAME,
                DATE,
                MERCHANT_CODE,
                AMOUNT_CENTS);

        verify(this.transactionDAO).selectByTransactionName(TRANSACTION_NAME);
        verify(this.transactionDAO).insert(any(Transaction.class));
    }

    /**
     * Exception thrown: wrong date format
     */
    @Test
    public void testPostList_wrongDateStringFormat_transactionServiceExceptionThrown() {
        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(TRANSACTION_NAME,
                        WRONG_DATE,
                        MERCHANT_CODE,
                        AMOUNT_CENTS));
        assertEquals(Status.WRONG_DATE_STRING_FORMAT, transactionException.getStatus());

    }

    /**
     * Exception thrown: wrong date range
     */
    @Test
    public void testPostList_dateOutOfRange_transactionServiceExceptionThrown() {
        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(TRANSACTION_NAME,
                        DATE,
                        MERCHANT_CODE,
                        NEGATIVE_AMOUNT_CENTS));
        assertEquals(Status.NEGATIVE_AMOUNT_CENTS, transactionException.getStatus());

    }

    /**
     * Exception thrown: negative amount cents
     */
    @Test
    public void testPostList_negativeAmountCents_transactionServiceExceptionThrown(){
        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(TRANSACTION_NAME,
                        OUT_RANGE_DATE,
                        MERCHANT_CODE,
                        AMOUNT_CENTS));
        assertEquals(Status.WRONG_DATE_STRING_FORMAT, transactionException.getStatus());

    }

    /**
     * Exception thrown: null merchant code
     */
    @Test
    public void testPostList_nullMerchantCode_transactionServiceExceptionThrown() {

        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(TRANSACTION_NAME,
                        DATE,
                        null,
                        AMOUNT_CENTS));
        assertEquals(Status.MERCHANT_CODE_IS_NULL, transactionException.getStatus());


    }

    /**
     * Exception thrown: null transaction name
     */
    @Test
    public void testPostList_nullTransactionName_transactionServiceExceptionThrown() throws Exception{

        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(null,
                        DATE,
                        MERCHANT_CODE,
                        AMOUNT_CENTS));
        assertEquals(Status.TRANSACTION_NAME_IS_NULL, transactionException.getStatus());

    }

    /**
     * Exception thrown: transactions already posted
     */
    @Test
    public void testPostList_transactionAlreadyPosted_transactionServiceExceptionThrown() throws Exception{

        Transaction transaction = Transaction.builder()
                .transactionName(TRANSACTION_NAME)
                .postYear(POST_YEAR)
                .postMonth(POST_MONTH)
                .postDay(POST_DAY)
                .merchantCode(MERCHANT_CODE)
                .amountCents(AMOUNT_CENTS)
                .build();

        when(this.transactionDAO.selectByTransactionName(TRANSACTION_NAME)).thenReturn(List.of(transaction));

        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.postOneTransaction(TRANSACTION_NAME,
                        DATE,
                        MERCHANT_CODE,
                        AMOUNT_CENTS));
        assertEquals(Status.TRANSACTION_ALREADY_POSTED, transactionException.getStatus());

        verify(this.transactionDAO).selectByTransactionName(TRANSACTION_NAME);

    }

    /**
     * Success: get monthly transaction list
     */
    @Test
    public void testGetMonthlyTransactionList_happyCase() throws Exception{

        Transaction transaction = Transaction.builder()
                .transactionName(TRANSACTION_NAME)
                .postYear(POST_YEAR)
                .postMonth(POST_MONTH)
                .postDay(POST_DAY)
                .merchantCode(MERCHANT_CODE)
                .amountCents(AMOUNT_CENTS)
                .build();
        // create a list of transaction
        when(this.transactionDAO.selectByYearMonth(POST_YEAR, POST_MONTH)).thenReturn(List.of(transaction));

        this.transactionService.getMonthlyTransactionList(POST_YEAR, POST_MONTH);

        verify(this.transactionDAO).selectByYearMonth(POST_YEAR, POST_MONTH);
    }

    /**
     * Exception thrown: transaction not found
     */
    @Test
    public void testGetMonthlyTransactionList_notFoundTransactionsGivenName_transactionServiceExceptionThrown() {

        when(this.transactionDAO.selectByYearMonth(POST_YEAR, POST_MONTH)).thenReturn(List.of());

        var transactionException = assertThrows(TransactionServiceException.class,
                () -> this.transactionService.getMonthlyTransactionList(POST_YEAR, POST_MONTH));

        assertEquals(Status.MONTH_NO_TRANSACTIONS, transactionException.getStatus());

        verify(this.transactionDAO).selectByYearMonth(POST_YEAR, POST_MONTH);
    }
}
