package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.project.handler.CustomAuthFailureHandler;
import com.project.ouath2.PrincipalOauth2UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity // 해당 설정을 FilterChain에 등록,
@Configuration 
public class SecurityConfig {

	private final PrincipalOauth2UserService principalOauth2UserService; // oAuth2로그인을 진행시켜주는 Service  , 생성자 주입
	
	 public static final String[] SECURITY_EXCLUDE_PATTERN_ARR = {
			 "/css/**", "/font/**", "/projectimg/**", "/favicon.ico",
				"/error", "/js/**"};
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // 비밀번호 복호화처리를 위한 Bean을 등록,
	}
 
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests() // 기본적으로권한이 존재하지 않는 비회원유저,일반유저 모두 접근이 가능한 URL을 설정
				.requestMatchers("/members/users","/index", "/index**", "/index/**","/","/products/{p_id}", "/products/options",
						"/products/options/{p_id}", "/reviews/**", "/comment", "/members/logout", "/search", "/category",
						"/members","/members/info","/members/info/**","/images/**","/reviews**","/reviews/**")
				.permitAll()
				// USER,SELLER에 대한 접근 권한설정
				.requestMatchers("/orders/**", "/orders", "/ordersheet", "/qnaboard/**","/members/put","/ordersheet**","/ordersheet/**").hasAnyRole("SELLER", "USER")
				.requestMatchers("/products/**", "/products/options/**", "/products/charts/**", "/products/charts**") //SELLER에 대한 권한설정
				.hasRole("SELLER").requestMatchers("/**").hasRole("ADMIN").anyRequest().authenticated(); //ADMIN은 모든 페이지에 대한 이동이 가능함.
																											
		http.formLogin() // LOGIN폼을 설정해줄때 작성 ,
				.loginPage("/members") // 로그인을 실행하게될 FORM페이지로의 이동
				.loginProcessingUrl("/members") // 해당하는 url로 접근시 로그인의 기능이 작동, request되는 값을 컨트롤러가 아닌 이곳으로 넘겨받음.
				.usernameParameter("m_email") // default= Username 파라미터로 넘어오는 값의 이름을 설정
				.passwordParameter("m_pwd")// default =Password
				.defaultSuccessUrl("/index",true) // 로그인 성공시 이동하는 페이지
				.failureHandler(failehandler()).permitAll().and().logout() // 로그아웃에 대한 설정
				.logoutUrl("/members/logout") // 로그아웃 처리 URL, default: /logout, 원칙적으로 post 방식만 지원
				.logoutSuccessUrl("/index") // 로그아웃 성공 후 이동페이지
				.permitAll().and().oauth2Login().loginPage("/members").defaultSuccessUrl("/index",true)
				.userInfoEndpoint().userService(principalOauth2UserService);
		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() { // css,js,img등의 시큐리티 필터적용이 필요없는 자원에 대한 접근을 설정,
		return (web) -> web.ignoring().requestMatchers(SECURITY_EXCLUDE_PATTERN_ARR); // 이부분은 Filter보다 먼저 적용된다,
	}

	@Bean
	public CustomAuthFailureHandler failehandler() { //로그인을 실패할 경우에 대한 핸들러 설정,
		return new CustomAuthFailureHandler();

	}

}