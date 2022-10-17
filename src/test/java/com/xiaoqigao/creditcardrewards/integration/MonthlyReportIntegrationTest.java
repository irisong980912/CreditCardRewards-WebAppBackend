package com.xiaoqigao.creditcardrewards.integration;

import com.xiaoqigao.creditcardrewards.DAO.TestTransactionDAO;
import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.model.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration test for calculating monthly report
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class MonthlyReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTransactionDAO testTransactionDAO;

    /** constants */
    private static final String YEAR = "2021";
    private static final String MONTH = "05";

    /**
     * Cleans up old data in the test database `rewards-test` before each test
     */
    @BeforeEach
    public void cleanUpOldData() {
        this.testTransactionDAO.deleteAll();
    }

    /**
     * Success: Test for successfully calculate rewards report for the given month
     */
    @Test
    public void testMonthlyRewardReport_happyCase() throws Exception {

        Transaction transaction = Transaction.builder()
                .transactionName("T01")
                .postYear("2021")
                .postMonth("05")
                .postDay("09")
                .merchantCode("sportcheck")
                .amountCents(2000)
                .build();

        // insert directly
        this.testTransactionDAO.insert(transaction);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200,\"message\":\"Successful Request.\"" +
                ",\"year\":\"2021\"," +
                "\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":75," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":75}" +
                "]}";

        assertEquals(exceptedResponse, actualResponse);
    }

    /**
     * Exception raised: Cannot calculate monthly report because there are no transactions in the given month.
     */
    @Test
    public void testMonthlyRewardReport_noTransactions() throws Exception {

        Transaction transaction = Transaction.builder()
                .transactionName("T03")
                .postYear("2020")
                .postMonth("01")
                .postDay("09")
                .merchantCode("whatever")
                .amountCents(1000)
                .build();
        // insert directly
        this.testTransactionDAO.insert(transaction);

        this.mockMvc.perform(get("/transaction/monthly-reward-report") // http client
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isBadRequest()) // HTTP status == 500
                .andExpect(header().string("Content-Type", "application/json")) // Content-Type = ?
                .andExpect(jsonPath("$.code").value(Status.MONTH_NO_TRANSACTIONS.getCode()))
                .andExpect(jsonPath("$.message").value(Status.MONTH_NO_TRANSACTIONS.getMessage()))
                .andReturn();
    }

    /** ================================== start of test cases ================================== */
    /**
     * Test for edge case: when the transaction amountCents are all zero.
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 0},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 0},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 0}]
     */
    @Test
    public void testMonthlyRewardReport_zeroCents() throws Exception { // apply rule4
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(0)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(0)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("subway").amountCents(0)
                .build();
        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":0," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":0}," +
                "{\"transaction_name\":\"T02\",\"point\":0}," +
                "{\"transaction_name\":\"T03\",\"point\":0}" +
                "]}";

        assertEquals(exceptedResponse, actualResponse);

    }

    /**
     * Test for edge case: when there are transactions from different months.
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2500},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 1000},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 1000},
     * {"transaction_name": "T04", "date": "2020-01-10", "merchant_code" : "sportcheck", "amount_cents": 10000}]
     */
    @Test
    public void testMonthlyRewardReport_differentMonths() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(2500)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(1000)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("subway").amountCents(1000)
                .build();

        Transaction t4 = Transaction.builder()
                .transactionName("T04")
                .postYear("2020").postMonth("01").postDay("09")
                .merchantCode("sportscheck").amountCents(10000)
                .build();
        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);
        this.testTransactionDAO.insert(t4);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":150," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":80}," +
                "{\"transaction_name\":\"T02\",\"point\":10}," +
                "{\"transaction_name\":\"T03\",\"point\":10}" +
                "]}";

        assertEquals(exceptedResponse, actualResponse);

    }

    /**
     * Test for edge case: when there are transactions from multiple merchants.
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2500},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 1000},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 1000}]
     */
    @Test
    public void testMonthlyRewardReport_multipleMerchants() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(2500)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(1000)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("subway").amountCents(1000)
                .build();
        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":150," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":80}," +
                "{\"transaction_name\":\"T02\",\"point\":10}," +
                "{\"transaction_name\":\"T03\",\"point\":10}" +
                "]}";

        assertEquals(exceptedResponse, actualResponse);

    }

    /**
     * Test for edge case: when there are transactions from multiple merchants and "other" merchants.
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2500},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 1000},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "whatever", "amount_cents": 500}]
     */
    @Test
    public void testMonthlyRewardReport_hasOtherMerchants() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(2500)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(1000)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("whatever").amountCents(500)
                .build();
        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":95," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":80}," +
                "{\"transaction_name\":\"T02\",\"point\":10}," +
                "{\"transaction_name\":\"T03\",\"point\":5}" +
                "]}";

        assertEquals(exceptedResponse, actualResponse);
    }

    /**
     * Test for edge case: when there are transactions from duplicated merchants
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2500},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "sportcheck", "amount_cents": 10000},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 2500},
     * {"transaction_name": "T04", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 2500}]
     */
    @Test
    public void testMonthlyRewardReport_duplicateMerchants() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(2500)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(10000)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(2500)
                .build();

        Transaction t4 = Transaction.builder()
                .transactionName("T04")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("subway").amountCents(2500)
                .build();

        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);
        this.testTransactionDAO.insert(t4);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report") // http client
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json")) // Content-Type = ?
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":660," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":80}," +
                "{\"transaction_name\":\"T02\",\"point\":375}," +
                "{\"transaction_name\":\"T03\",\"point\":25}," +
                "{\"transaction_name\":\"T04\",\"point\":25}" +
                "]}";
        assertEquals(exceptedResponse, actualResponse);
    }

    /**
     * Test for edge case: when there are duplicated merchants and cents remaining
     * [{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2500},
     * {"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "sportcheck", "amount_cents": 10068},
     * {"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 3550},
     * {"transaction_name": "T04", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 3558},
     * {"transaction_name": "T04", "date": "2021-05-10", "merchant_code" : "whatever", "amount_cents": 1000}]
     */
    @Test
    public void testMonthlyRewardReport_hasDuplicatedCentsRemaining() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(2500)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(10068)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("tim_hortons").amountCents(3550)
                .build();

        Transaction t4 = Transaction.builder()
                .transactionName("T04")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("subway").amountCents(3558)
                .build();

        Transaction t5 = Transaction.builder()
                .transactionName("T05")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("whatever").amountCents(1000)
                .build();

        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);
        this.testTransactionDAO.insert(t4);
        this.testTransactionDAO.insert(t5);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":741," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":80}," +
                "{\"transaction_name\":\"T02\",\"point\":375}," +
                "{\"transaction_name\":\"T03\",\"point\":35}," +
                "{\"transaction_name\":\"T04\",\"point\":35}," +
                "{\"transaction_name\":\"T05\",\"point\":10}" +
                "]}";
        assertEquals(exceptedResponse, actualResponse);
    }

    /**
     * Test for the case stated in the handout.
     */
    @Test
    public void testMonthlyRewardReport_exampleTransactions() throws Exception {
        Transaction t1 = Transaction.builder()
                .transactionName("T01")
                .postYear("2021").postMonth("05").postDay("01")
                .merchantCode("sportcheck").amountCents(21000)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionName("T02")
                .postYear("2021").postMonth("05").postDay("02")
                .merchantCode("sportcheck").amountCents(8700)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionName("T03")
                .postYear("2021").postMonth("05").postDay("03")
                .merchantCode("tim_hortons").amountCents(323)
                .build();

        Transaction t4 = Transaction.builder()
                .transactionName("T04")
                .postYear("2021").postMonth("05").postDay("04")
                .merchantCode("tim_hortons").amountCents(1267)
                .build();

        Transaction t5 = Transaction.builder()
                .transactionName("T05")
                .postYear("2021").postMonth("05").postDay("05")
                .merchantCode("tim_hortons").amountCents(2116)
                .build();

        Transaction t6 = Transaction.builder()
                .transactionName("T06")
                .postYear("2021").postMonth("05").postDay("06")
                .merchantCode("tim_hortons").amountCents(2211)
                .build();

        Transaction t7 = Transaction.builder()
                .transactionName("T07")
                .postYear("2021").postMonth("05").postDay("07")
                .merchantCode("subway").amountCents(1853)
                .build();

        Transaction t8 = Transaction.builder()
                .transactionName("T08")
                .postYear("2021").postMonth("05").postDay("08")
                .merchantCode("subway").amountCents(2153)
                .build();

        Transaction t9 = Transaction.builder()
                .transactionName("T09")
                .postYear("2021").postMonth("05").postDay("09")
                .merchantCode("sportcheck").amountCents(7326)
                .build();

        Transaction t10 = Transaction.builder()
                .transactionName("T10")
                .postYear("2021").postMonth("05").postDay("10")
                .merchantCode("tim_hortons").amountCents(1321)
                .build();

        // insert directly
        this.testTransactionDAO.insert(t1);
        this.testTransactionDAO.insert(t2);
        this.testTransactionDAO.insert(t3);
        this.testTransactionDAO.insert(t4);
        this.testTransactionDAO.insert(t5);
        this.testTransactionDAO.insert(t6);
        this.testTransactionDAO.insert(t7);
        this.testTransactionDAO.insert(t8);
        this.testTransactionDAO.insert(t9);
        this.testTransactionDAO.insert(t10);

        MvcResult result = this.mockMvc.perform(get("/transaction/monthly-reward-report")
                        .param("year", YEAR)
                        .param("month", MONTH))
                .andExpect(status().isOk()) // HTTP status == 200
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.code").value(Status.OK.getCode()))
                .andExpect(jsonPath("$.message").value(Status.OK.getMessage()))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String exceptedResponse = "{\"code\":1200," +
                "\"message\":\"Successful Request.\"," +
                "\"year\":\"2021\",\"month\":\"05\"," +
                "\"maximum_monthly_rewards_point\":1657," +
                "\"transaction_level_points_list\":[" +
                "{\"transaction_name\":\"T01\",\"point\":760}," +
                "{\"transaction_name\":\"T02\",\"point\":307}," +
                "{\"transaction_name\":\"T03\",\"point\":3}," +
                "{\"transaction_name\":\"T04\",\"point\":12}," +
                "{\"transaction_name\":\"T05\",\"point\":21}," +
                "{\"transaction_name\":\"T06\",\"point\":22}," +
                "{\"transaction_name\":\"T07\",\"point\":18}," +
                "{\"transaction_name\":\"T08\",\"point\":21}," +
                "{\"transaction_name\":\"T09\",\"point\":238}," +
                "{\"transaction_name\":\"T10\",\"point\":13}" +
                "]}";
        assertEquals(exceptedResponse, actualResponse);
    }
    /** ================================== end of test cases ================================== */


}
