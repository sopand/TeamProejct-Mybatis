package com.project.handler;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	// SimpleUrlAuthenticationFailureHandler를 상속받아 로그인단계에서 이상이 발생할 경우 이곳으로 오게 된다.

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
	//로그인 실패에대한 처리를 위한 메서드
		
		// 로그인 실패 사유에 대한 메시지를 담아놓기 위한 Stirng 객체
		String errorMessage;
		
		
		//각각 에러에 대한 메시지 코드의 작성
		if (exception instanceof BadCredentialsException) {
			errorMessage = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해주세요.";
		} else if (exception instanceof InternalAuthenticationServiceException) {
			errorMessage = "해당 아이디와 비밀번호는 찾을수 없는 정보입니다.";
		} else if (exception instanceof UsernameNotFoundException) {
			errorMessage = "존재하지 않는 계정입니다. 회원가입 후 로그인해주세요.";
		} else {
			errorMessage = "알 수 없는 오류로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요.";
		}
		
		//String 객체를 UTF로 인코딩처리 ( 한글 깨짐문제를 방지하기 위함 )
		errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
		//실패시 이동하는 URL로 이동하게 됩니다. error에대한 정보를 가진체로 이동!
		setDefaultFailureUrl("/members?error=true&exception=" + errorMessage);
		super.onAuthenticationFailure(request, response, exception);
	}
}