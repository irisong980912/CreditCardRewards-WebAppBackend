package com.xiaoqigao.creditcardrewards.response;

import com.xiaoqigao.creditcardrewards.enums.Status;

import lombok.Data;

@Data
public class CommonResponse {
    int code;
    String message;

    public CommonResponse(Status status) {
        this.code = status.getCode();
        this.message = status.getMessage();
    }

    public CommonResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
