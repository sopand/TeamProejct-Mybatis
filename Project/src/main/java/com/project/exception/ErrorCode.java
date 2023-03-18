package com.project.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode { // 전체적인 예외 관리를 쉽게하기 위해 정해놓은 에러코드를 저장하는 Enum 클래스
	// 400 BAD_REQUEST: 잘못된 요청
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	

	// 404 NOT_FOUND: 리소스를 찾을 수 없음
	POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),

	// 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),

	// 500 INTERNAL_SERVER_ERROR: 내부 서버 오류
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),

	;
	
	// 예외에 대한 상태코드(Status)와 이름(Error)을 처리하는 데 사용,
	private final HttpStatus status;
	// 예외에 대한 응답 메시지를 처리하는데 사용하는 멤버
	private final String message;

}