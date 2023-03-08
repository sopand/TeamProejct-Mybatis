package com.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.catalina.filters.ExpiresFilter.XServletOutputStream;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.model.Member;
import com.project.service.EmailService;
import com.project.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/members") // member
public class MemberController {

	@Autowired
	private final MemberService mService;

	@Autowired
	private final EmailService eService;


	@GetMapping("") 
	public String loginForm(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "exception", required = false) String exception,Model model) {  
		if(error!=null) {
			model.addAttribute("exception",exception);
		}
		return "loginForm";
	}
	@GetMapping("/users")  
	public String joinForm() {
		return "joinForm";
	}

	@PostMapping("/users")
	public String join(Member member) {
		mService.MemberAdd(member);
		return "redirect:/member/index";
	}


	@GetMapping("/put") 
	public String updateForm(Principal principal, Model model) {
		Member member = mService.FindID(principal.getName());
		model.addAttribute("member", member);
		String address = mService.FindAddr(principal.getName());
		if (address != null) {
			String[] addr = address.split("/");

			switch (addr.length) {
			case 2:
				model.addAttribute("addr1", addr[0]);
				model.addAttribute("addr2", addr[1]);
				break;
			case 3:
				model.addAttribute("addr1", addr[0]);
				model.addAttribute("addr2", addr[1]);
				model.addAttribute("addr3", addr[2]);
				break;
			}
		}
		return "memberUpdate";
	}

	@PutMapping("/put")
	public String update(Member member, @RequestParam("addr1") String addr1, @RequestParam("addr2") String addr2,
			@RequestParam(required = false) String addr3) {
		String address = addr1 + "/" + addr2 + "/" + addr3;
		member.setM_address(address);
		mService.editMember(member);
		return "index";
	}

	@DeleteMapping("/info")
	public String delete(HttpSession session, Member m) {
		System.out.println(m);
		mService.delete(m);
		session.invalidate();
		return "index";
	}

	@GetMapping("/info") 
	public String findPwdForm() {
		return "findPwd";
	}

	@PutMapping("/info") 
	public String pwdUpDate(Member member, @RequestParam("m_email") String m_eamil) {
		mService.changePwd(member);
		System.out.println(member);
		return "loginForm";
	}

	@ResponseBody
	@PostMapping("/info") 
	public String findPwd(@RequestParam("m_email") String m_email, @RequestParam("m_name") String m_name) {
		System.out.println(m_email);
		System.out.println(m_name);
		String user = mService.FindPwd(m_email, m_name);
		return user;
	}

	/* 이메일 중복확인 */
	@ResponseBody
	@PostMapping("/info/email") 
	public int emailCheck(@RequestParam("m_email") String m_email) {
		int cnt = mService.emailCheck(m_email);
		return cnt;
	}

	/* 닉네임 중복확인 */
	@ResponseBody  
	@PostMapping("/info/nick") 
	public int nickCheck(@RequestParam("m_nickname") String m_nickname) {
		int cnt2 = mService.nickCheck(m_nickname);
		return cnt2;
	}

	/* 인증번호 발송 */
	@ResponseBody  
	@PostMapping("/info/send") 
	public String emailCheck(String m_email, Model model) throws Exception {
		String code = eService.sendSimpleMessage(m_email); // EmailService의 메일을 보내는 메서드를 작동.(리턴값으로 인증번호를 받아옴)
		model.addAttribute("email", m_email); // 인증번호를 보낸 email의 값을 인증 체크시 사용하기 위해 View로 전송 (Hidden으로 값을 저장해둠)
		return code;
	}
	
	@ResponseBody
	@PostMapping("/info/type") 
	public String typeCheck(@RequestParam("m_email") String m_email) {
		 String cnt3 = mService.FindType(m_email);
		return cnt3;

}
}
