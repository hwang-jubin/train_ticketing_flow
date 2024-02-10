package com.example.train_ticketing_flow.exception;

import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

// 여러 컨트롤러에서 발생하는 예외를 일관된 방식으로 처리
@RestControllerAdvice
public class ApplicationAdvice {

    @ExceptionHandler
    Mono<ResponseEntity<ServerExceptionResponse>> applicationExceptionHandler(ApplicationException ex){
        return Mono.just(
                ResponseEntity
                        .status(ex.getHttpStatus())
                        .body(new ServerExceptionResponse(ex.getCode(),ex.getReason())));
    }
    public record ServerExceptionResponse(String code, String reason){

    }
}
