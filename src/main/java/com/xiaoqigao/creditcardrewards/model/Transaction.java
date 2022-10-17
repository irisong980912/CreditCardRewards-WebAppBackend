package com.xiaoqigao.creditcardrewards.model;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

// save according to year and month

@Value
@Builder
public class Transaction {
    int id; // primary key
    String transactionName;
    String postYear;
    String postMonth;
    String postDay;
    String merchantCode;
    int amountCents;
}
