package com.tokkitalk.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tokkitalk.model.MemberDAO;
import com.tokkitalk.model.MevenMember;

@WebServlet("/test")
public class TestController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("=== 테스트 컨트롤러 시작 ===");
		
		try {
			MemberDAO dao = new MemberDAO();
			System.out.println("DAO 객체 생성 완료");
			
			// 기존 데이터 삭제 (중복 방지..)
			System.out.println("기존 테스트 데이터 삭제 중...");
			int deleteResult = dao.deleteTestData();
			System.out.println("삭제된 행 수: " + deleteResult);
			
			// 전체 데이터 조회 테스트
			List<MevenMember> allMembers = dao.testSelectAll();
			System.out.println("현재 전체 회원 수: " + allMembers.size());
			
			// 테스트 데이터 강제 삽입
			System.out.println("테스트 데이터를 강제로 삽입합니다...");
			MevenMember testMember = new MevenMember("test", "1234", "테스터", "F", new java.util.Date());
			int result = dao.join(testMember);
			System.out.println("테스트 데이터 삽입 결과: " + (result > 0 ? "성공" : "실패"));
			
			// 다시 조회
			allMembers = dao.testSelectAll();
			System.out.println("삽입 후 전체 회원 수: " + allMembers.size());
			
			for (MevenMember member : allMembers) {
				System.out.println("회원 정보:");
				System.out.println("- ID: " + member.getUser_id());
				System.out.println("- PW: " + member.getUser_pw());
				System.out.println("- NAME: " + member.getUser_name());
				System.out.println("- GENDER: " + member.getGender());
				System.out.println("- DATE: " + member.getUser_date());
				System.out.println("---");
			}
			
			response.getWriter().write("테스트 완료! 콘솔을 확인하세요. 전체 회원 수: " + allMembers.size());
			
		} catch (Exception e) {
			System.out.println("테스트 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.getWriter().write("오류 발생: " + e.getMessage());
		}
	}
}
