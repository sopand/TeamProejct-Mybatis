package com.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException { // 런타임 에러가 발생할 경우 이곳으로 오게됩니다.

    private final ErrorCode errorCode; 

}