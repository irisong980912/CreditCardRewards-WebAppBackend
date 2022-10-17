package com.xiaoqigao.creditcardrewards.exception;

import com.xiaoqigao.creditcardrewards.enums.Status;

public class TransactionServiceException extends Exception{

    private Status status;

    public TransactionServiceException(Status status) {
        super(status.getMessage());
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
