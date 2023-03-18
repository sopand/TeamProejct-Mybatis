package com.project.exception;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now(); // 에러의 발생 시간을 출력하기위함
    private final int status;  // 에러의 상태코드를 출력 ex) 404
    private final String error; // 에러의 상태코드의 Error 문자 출력 ex) Not Found
    private final String code; 
    private final String message; // 에러에 대한 지정해놓은 메시지가 출력

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

}