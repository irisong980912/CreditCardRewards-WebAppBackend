package com.xiaoqigao.creditcardrewards.integration;

import com.xiaoqigao.creditcardrewards.DAO.*;
import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration test for posting a list of monthly transactions
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class PostListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTransactionDAO testTransactionDAO;

    /**
     * Cleans up old data in the test database `rewards-test` before each test
     */
    @BeforeEach
    public void cleanUpOldData() {
        this.testTransactionDAO.deleteAll();
    }

    /**
     * Success: Test for successfully posting a list of transactions
     */
    @Test
    public void testPostList_happyCase() throws Exception {

        var requestBody = "[{\"transaction_name\": \"T01\", " +
                "\"date\": \"2021-05-09\", " +
                "\"merchant_code\" : \"sportcheck\", " +
                "\"amount_cents\": 2550}]";

        // mockMVC post the request
        this.mockMvc.perform(post("/transaction/post-list")
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()));

        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T01");
        assertNotNull(transactions);
        assertEquals(1, transactions.size()); // expected, actual

        Transaction transaction = transactions.get(0);

        // check if the fields are matching
        assertEquals("T01", transaction.getTransactionName());
        assertEquals("2021", transaction.getPostYear());
        assertEquals("05", transaction.getPostMonth());
        assertEquals("09", transaction.getPostDay());
        assertEquals("sportcheck", transaction.getMerchantCode());
        assertEquals(2550, transaction.getAmountCents());
    }

    /**
     * Exception thrown: wrong date format
     */
    @Test
    public void testPostList_wrongDateStringFormat() throws Exception {

        var requestBody = "[{\"transaction_name\": \"T02\", " +
                "\"date\": \"2021/05/09\", " +
                "\"merchant_code\" : \"whatever\", " +
                "\"amount_cents\": 1000}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.WRONG_DATE_STRING_FORMAT.getCode()))
                .andExpect(jsonPath("$.message").value(Status.WRONG_DATE_STRING_FORMAT.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T02");
        assertNotNull(transactions); // should not be null
        assertTrue(transactions.isEmpty()); // should not add to database
    }

    /**
     * Exception thrown: wrong date range
     */
    @Test
    public void testPostList_dateOutOfRange() throws Exception {

        var requestBody = "[{\"transaction_name\": \"T02\", " +
                "\"date\": \"2021-90-51\", " +
                "\"merchant_code\" : \"whatever\", " +
                "\"amount_cents\": 1000}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.WRONG_DATE_STRING_FORMAT.getCode()))
                .andExpect(jsonPath("$.message").value(Status.WRONG_DATE_STRING_FORMAT.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T02");
        assertNotNull(transactions); // should not be null
        assertTrue(transactions.isEmpty()); // should not add to database
    }

    /**
     * Exception thrown: negative amount cents
     */
    @Test
    public void testPostList_negativeAmountCents() throws Exception {

        var requestBody = "[{\"transaction_name\": \"T02\", " +
                "\"date\": \"2021-05-09\", " +
                "\"merchant_code\" : \"whatever\", " +
                "\"amount_cents\": -100}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.NEGATIVE_AMOUNT_CENTS.getCode()))
                .andExpect(jsonPath("$.message").value(Status.NEGATIVE_AMOUNT_CENTS.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T02");
        assertNotNull(transactions); // should not be null
        assertTrue(transactions.isEmpty()); // should not add to database
    }

    /**
     * Exception thrown: null merchant code
     */
    @Test
    public void testPostList_nullMerchantCode() throws Exception {

        var requestBody = "[{\"transaction_name\": \"T02\", " +
                "\"date\": \"2021-05-09\", " +
                "\"merchant_code\" : null, " +
                "\"amount_cents\": 100}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json")) // Content-Type = ?
                .andExpect(jsonPath("$.code").value(Status.MERCHANT_CODE_IS_NULL.getCode()))
                .andExpect(jsonPath("$.message").value(Status.MERCHANT_CODE_IS_NULL.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T02");
        assertNotNull(transactions); // should not be null
        assertTrue(transactions.isEmpty()); // should not add to database
    }

    /**
     * Exception thrown: null transaction name
     */
    @Test
    public void testPostList_nullTransactionName() throws Exception {

        var requestBody = "[{\"transaction_name\": null, " +
                "\"date\": \"2021-05-09\", " +
                "\"merchant_code\" : \"whatever\", " +
                "\"amount_cents\": 100}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json")) // Content-Type = ?
                .andExpect(jsonPath("$.code").value(Status.TRANSACTION_NAME_IS_NULL.getCode()))
                .andExpect(jsonPath("$.message").value(Status.TRANSACTION_NAME_IS_NULL.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName(null);
        assertNotNull(transactions); // should not be null
        assertTrue(transactions.isEmpty()); // should not add to database
    }

    /**
     * Exception thrown: transactions already posted
     */
    @Test
    public void testPostList_transactionAlreadyPosted() throws Exception {

        Transaction transaction = Transaction.builder()
                .transactionName("T03")
                .postYear("2021")
                .postMonth("05")
                .postDay("09")
                .merchantCode("whatever")
                .amountCents(1000)
                .build();
        // insert one record first
        this.testTransactionDAO.insert(transaction);

        var requestBody = "[{\"transaction_name\": \"T03\", " +
                "\"date\": \"2021/05/09\", " +
                "\"merchant_code\" : \"whatever\", " +
                "\"amount_cents\": 1000}]";

        this.mockMvc.perform(post("/transaction/post-list") // http client
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.TRANSACTION_ALREADY_POSTED.getCode()))
                .andExpect(jsonPath("$.message").value(Status.TRANSACTION_ALREADY_POSTED.getMessage()));


        List<Transaction> transactions = this.testTransactionDAO.selectByTransactionName("T03");
        assertNotNull(transactions); // should not be null
        assertEquals(1, transactions.size()); // expected, actual
    }
}
















