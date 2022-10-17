package com.xiaoqigao.creditcardrewards.advice;

import com.xiaoqigao.creditcardrewards.exception.TransactionServiceException;
import com.xiaoqigao.creditcardrewards.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(TransactionServiceException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse> handleTransactionServiceException(TransactionServiceException exception) {
        var commonResponse = new CommonResponse(exception.getStatus());
        return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<CommonResponse> handleException(Exception exception) {
        var commonResponse = new CommonResponse(9999, exception.getMessage());
        return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
