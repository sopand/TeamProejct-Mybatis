package com.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice // 컨트롤러 전역에서 발생할 수 있는 예외를 찾아 Throw, 
@Slf4j // 롬복에서 제공해주는 기능, 해당 어노테이션이 선언된 클래스에 자동으로 로그 객체를 생성해준다.
public class GlobalExceptionHandler {
	
	
	@ExceptionHandler(CustomException.class) // @ExceptionHandler에 지정된 예외와 동일한 예외가 발생시 이 메서드를 실행 시키기 위해 사용하는 어노테이션입니다.
	protected ResponseEntity<ErrorResponse> handleCustomException(final CustomException e) {
		log.error("handleCustomException: {}", e.getErrorCode());
		return ResponseEntity.status(e.getErrorCode().getStatus().value()).body(new ErrorResponse(e.getErrorCode()));
	}
	//ResponseEntity<ErrorResponse> = HttpRequest에 대한 응답 데이터를 포함하는 클래스,
	// <Type>에 해당하는 데이터와 HTTP 상태코드를 함께 리턴해준다.
	// 예외가 발생 했을 때, ErrorResponse 형식으로 예외 정보를 Response로 내려준다.
	
	
	//HTTP 405 Exception
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class) 
	protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
			final HttpRequestMethodNotSupportedException e) {
		log.error("handleHttpRequestMethodNotSupportedException: {}", e.getMessage());
		return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value())
				.body(new ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED));
	}
	
	//HTTP 500 Exception
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
		log.error("handleException: {}", e.getMessage());
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
				.body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}