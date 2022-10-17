package com.xiaoqigao.creditcardrewards.exception;

import com.xiaoqigao.creditcardrewards.enums.Status;
import lombok.Value;

/**
 * Customized exception
 */
@Value
public class TransactionServiceException extends Exception{

    private Status status;

    public TransactionServiceException(Status status) {
        super(status.getMessage());
        this.status = status;
    }

}
