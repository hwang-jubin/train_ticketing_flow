package com.example.train_ticketing_flow.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001","already registered in queue");
    private HttpStatus httpStatus;
    private String code;
    private String reason;

    public ApplicationException build(){

    return new ApplicationException(httpStatus, code, reason);
    }
}
