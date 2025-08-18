package com.tokkitalk.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MemberDAO {
	private SqlSessionFactory sqlSessionFactory;
	
	public MemberDAO() {
		try {
			String resource = "mybatis-db.xml";
			InputStream inputStream = Resources.getResourceAsStream(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 회원가입
	public int join(MevenMember member) {
		SqlSession session = sqlSessionFactory.openSession(true);
		int cnt = 0;
		try {
			cnt = session.insert("com.tokkitalk.db.MemberMapper.join", member);
		} finally {
			session.close();
		}
		return cnt;
	}
	
	// 로그인
	public MevenMember login(MevenMember member) {
		System.out.println("=== DAO 로그인 시작 ===");
		System.out.println("전달받은 ID: " + member.getUser_id());
		System.out.println("전달받은 PW: " + member.getUser_pw());
		
		SqlSession session = sqlSessionFactory.openSession();
		MevenMember result = null;
		try {
			System.out.println("SQL 실행 시작...");
			result = session.selectOne("com.tokkitalk.db.MemberMapper.login", member);
			System.out.println("SQL 실행 완료");
			System.out.println("조회 결과: " + (result != null ? "데이터 있음" : "데이터 없음"));
			
			if (result != null) {
				System.out.println("조회된 데이터:");
				System.out.println("- USER_ID: " + result.getUser_id());
				System.out.println("- USER_PW: " + result.getUser_pw());
				System.out.println("- USER_NAME: " + result.getUser_name());
				System.out.println("- GENDER: " + result.getGender());
				System.out.println("- USER_DATE: " + result.getUser_date());
			}
		} catch (Exception e) {
			System.out.println("=== 오류 발생 ===");
			System.out.println("오류 메시지: " + e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
			System.out.println("세션 종료");
		}
		return result;
	}
	
	// 회원 정보 조회
	public MevenMember selectMember(String id) {
		SqlSession session = sqlSessionFactory.openSession();
		MevenMember member = null;
		try {
			member = session.selectOne("com.tokkitalk.db.MemberMapper.selectMember", id);
		} finally {
			session.close();
		}
		return member;
	}

	// 아이디 중복 확인
	public boolean existsById(String userId) {
		SqlSession session = sqlSessionFactory.openSession();
		try {
			Integer count = session.selectOne("com.tokkitalk.db.MemberMapper.existsById", userId);
			return count != null && count > 0;
		} finally {
			session.close();
		}
	}
	
	// 전체 회원 조회
	public List<MevenMember> selectAll() {
		SqlSession session = sqlSessionFactory.openSession();
		List<MevenMember> list = null;
		try {
			list = session.selectList("com.tokkitalk.db.MemberMapper.selectAll");
		} finally {
			session.close();
		}
		return list;
	}
	
	// 테스트용 전체 데이터 조회
	public List<MevenMember> testSelectAll() {
		SqlSession session = sqlSessionFactory.openSession();
		List<MevenMember> list = null;
		try {
			list = session.selectList("com.tokkitalk.db.MemberMapper.testSelectAll");
		} finally {
			session.close();
		}
		return list;
	}
	
	// 테스트용 데이터 삭제
	public int deleteTestData() {
		SqlSession session = sqlSessionFactory.openSession(true);
		int result = 0;
		try {
			result = session.delete("com.tokkitalk.db.MemberMapper.deleteTestData");
		} finally {
			session.close();
		}
		return result;
	}
}
