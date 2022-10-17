package com.xiaoqigao.creditcardrewards.controller;

import com.xiaoqigao.creditcardrewards.enums.Status;
import com.xiaoqigao.creditcardrewards.model.Transaction;
import com.xiaoqigao.creditcardrewards.request.PostTransRequest;
import com.xiaoqigao.creditcardrewards.response.*;
import com.xiaoqigao.creditcardrewards.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/transaction")
public class transactionController {

    @Autowired
    private TransactionService transactionService;

    @DeleteMapping("/")
    public CommonResponse deleteAll() {
        this.transactionService.deleteAll();

        return new CommonResponse(Status.OK);
    }

    @PostMapping("/hello")
    public String helloWorld(@RequestParam(required = false, defaultValue = "Alice") String name,
                             @RequestHeader("User-Agent") String userAgent,
                             @RequestBody String body) {
        return "hello " + name + " from " + userAgent + " body: " + body;
    }

    // post multiple
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
    @GetMapping("/monthly-reward-report")
    public MonthlyReportResponse getMonthlyReport(@RequestParam(required = true) String year,
                                                  @RequestParam(required = true) String month) throws Exception {

        int maxPoint =  this.transactionService.getMonthlyMaxPoint(year, month);
        List<Transaction> transactionList = this.transactionService.getMonthlyTransactionList(year, month);

        List<TransactionLevelPointResponse> levelPointList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            String transactionName = transaction.getTransactionName();
            int maxLevelPoint =  this.transactionService.getTransactionLevelPointByName(transactionName);
            levelPointList.add( new TransactionLevelPointResponse(transactionName, maxLevelPoint));
        }

        return new MonthlyReportResponse(year, month, maxPoint, levelPointList);

    }
}
