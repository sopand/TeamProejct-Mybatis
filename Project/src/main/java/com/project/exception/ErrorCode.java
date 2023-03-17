package com.project.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode { // 각각의 에러코드에 대한 상수값들을 저장해놓은 Enum클래스입니다.
	// 400 BAD_REQUEST: 잘못된 요청
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	

	// 404 NOT_FOUND: 리소스를 찾을 수 없음
	POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),

	// 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),

	// 500 INTERNAL_SERVER_ERROR: 내부 서버 오류
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),

	;
	
	// Enum 각각의 값에서 1번쨰에 해당하는 HttpStatus가이곳에 할당.
	private final HttpStatus status;
	// Enum 각각의 값에서 2번째인 String 객체들이 이곳에 할당.
	private final String message;

}