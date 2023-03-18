package com.project.ouath2;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.project.mapper.MemberMapper;
import com.project.model.Member;
import com.project.service.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
	
	private final MemberMapper memberMapper;

	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest); // oAuth2로그인 방식으로 로그인 시도를 했을때 로그인시도한 정보를 가지고 있는 객체,
		String m_email = oAuth2User.getAttribute("email"); // 로그인 정보에서 email을가져옴
		String m_name = oAuth2User.getAttribute("name"); // 로그인 정보에서 이름을 가져옴.
		String m_type = "ROLE_USER";  // 기본 권한인 USER권한을 입력
		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // oAuth2 로그인시 비밀번호가 필요없지만 로그인 아이디가 존재하지 않을경우 아이디 생성에 필요한 비밀번호를 기본값으로 생성해주기 위함
		String m_pwd = passwordEncoder.encode("비밀번호제작"); // 아이디가 없을시 아이디 생성의 기본 비밀번호 값
		Member newMember = Member.builder().m_email(m_email).m_name(m_name).m_type(m_type).m_pwd(m_pwd).build(); //위의 정보들을 member 객체에 저장.
		Member memberCheck = memberMapper.FindID(m_email); // 해당하는 아이디정보가 DB에 존재하는 값인지 확인 하기 위함.
		if (memberCheck == null) { // 존재하지 않을경우
			memberMapper.oAuthAdd(newMember); // 회원가입 처리를 진행시킨다.
		}else {
			newMember.setM_type(memberCheck.getM_type()); // 존재한다면 그 회원의 권한을 가져와 적용시킨다.
		}
		return new PrincipalDetails(newMember, oAuth2User.getAttributes()); // 로그인 작업을 진행, Oauth2 정보를 같이 보내준다.
	}

}
