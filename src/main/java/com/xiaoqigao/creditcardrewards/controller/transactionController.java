package com.xiaoqigao.creditcardrewards.controller;

import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.model.RewardsReport;
import com.xiaoqigao.creditcardrewards.model.Transaction;
import com.xiaoqigao.creditcardrewards.request.PostTransRequest;
import com.xiaoqigao.creditcardrewards.response.*;
import com.xiaoqigao.creditcardrewards.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class transactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * A POST request for posting a list of transactions
     * @param listTransRequest a list of PostTransRequest
     * @return status OK
     */
    @PostMapping("/post-list")
    public CommonResponse postListTransaction(@RequestBody List<PostTransRequest> listTransRequest) throws Exception {

        for (PostTransRequest postTransRequest : listTransRequest) {

            this.transactionService.postOneTransaction(postTransRequest.getTransactionName(),
                    postTransRequest.getDate(),
                    postTransRequest.getMerchantCode(),
                    postTransRequest.getAmountCents());
        }

        return new CommonResponse(Status.OK);
    }

    /**
     * A GET request for the reward point report given the year and month
     * @param year posting year
     * @param month posting month
     * @return MonthlyReportResponse
     */
    @GetMapping("/monthly-reward-report")
    public MonthlyReportResponse getMonthlyReport(@RequestParam(required = true) String year,
                                                  @RequestParam(required = true) String month) throws Exception {


        List<Transaction> transactionList = this.transactionService.getMonthlyTransactionList(year, month);
        RewardsReport rewardsReport = this.transactionService.getMonthlyRewardsReport(transactionList);

        return new MonthlyReportResponse(year, month, rewardsReport.getMonthlyRewardsPoint(), levelPointList);

    }
}
