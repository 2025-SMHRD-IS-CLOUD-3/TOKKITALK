package com.tokkitalk.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.tokkitalk.model.MemberDAO;
import com.tokkitalk.model.MevenMember;

@WebServlet("/Update")
public class Update extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 세션에서 현재 로그인된 사용자 정보 가져오기
        HttpSession session = request.getSession();
        MevenMember existingMember = (MevenMember) session.getAttribute("member");

        // 세션 정보가 없으면, 로그인되지 않은 상태이므로 로그인 페이지로 리다이렉트
        if (existingMember == null) {
            System.out.println("로그인되지 않은 사용자입니다.");
            response.sendRedirect("main.html?msg=not_logged_in");
            return;
        }

        // 2. 파라미터 수집
        String user_id = request.getParameter("id");
        String user_pw = request.getParameter("pw");
        String user_name = request.getParameter("name");
        String gender = request.getParameter("gender");
        String user_date = request.getParameter("user_date");
        
        // 3. 비밀번호 일치 여부 확인 (보안 강화)
        // 사용자가 입력한 비밀번호와 기존 비밀번호가 일치하는지 확인
        if (!user_pw.equals(existingMember.getUser_pw())) {
            System.out.println("비밀번호 불일치. 업데이트 실패.");
            response.sendRedirect("myPage.jsp?msg=password_mismatch");
            return;
        }
        
        // 4. 업데이트할 회원 정보 객체 생성
        MevenMember updatedMember = new MevenMember(user_id, user_pw, user_name, null, null);

        // 5. DAO를 통해 데이터베이스 업데이트 실행
        MemberDAO dao = new MemberDAO();
        int cnt = dao.update(updatedMember);
        
        // 6. 업데이트 결과 판별 및 세션 갱신
        if (cnt > 0) {
            System.out.println("회원 업데이트 성공!");
            // 데이터베이스 업데이트 성공 시 세션 정보도 최신으로 갱신
            session.setAttribute("member", updatedMember);
            response.sendRedirect("MyPage.html?msg=update_success");
        } else {
            System.out.println("회원 업데이트 실패");
            response.sendRedirect("MyPage.html?msg=update_fail");
        }
    }
}