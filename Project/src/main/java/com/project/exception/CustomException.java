package com.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // Unchecked Exception인 RuntimeException을 상속받는다.
public class CustomException extends RuntimeException { // 런타임 에러가 발생할 경우 이곳으로 오게됩니다.
					//개발자가 ErrorCode에 직접 정의한 Custom 예외를 처리할 Exception 클래스입니다.
    private final ErrorCode errorCode; 

}