package com.project.mapper;
import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.model.Member;

@Mapper
public interface MemberMapper {
	
public void MemberAdd(Member m);
	
	public void oAuthAdd(Member m);
	public Member FindID(String m_email); 
	
	public String FindAddr(String m_email);
	
	public void EditMember(Member m);
	
	public void Delete(Member m);
	
	public void ChangePwd(Member m);

	public String FindPwd(@Param("m_email")String m_email, @Param("m_name")String m_name);

	public void EmailNum(@Param("m_code")String m_code,@Param("m_email")String email);
	
	public String EmailCode(@Param("m_code")String m_code,@Param("m_email")String email);
	
	public ArrayList<Member> AllMember();
	
	public int EmailCheck(String m_email);
	
	public int NickCheck(String m_nickname);
	
	public String FindType(String m_email);
}
