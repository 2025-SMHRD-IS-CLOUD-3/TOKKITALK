package com.tokkitalk.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.tokkitalk.model.MavenMember;
import com.tokkitalk.model.MemberDAO;

@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. main.jsp에서 로그인 정보를 받기 전에 인코딩 설정
        request.setCharacterEncoding("UTF-8");

        // 2. 로그인 폼에서 아이디와 비밀번호 받기
        String id = request.getParameter("id");   // 아이디
        String pw = request.getParameter("pw");   // 비밀번호

        // 3. MavenMember 객체 생성 후 MemberDAO에 로그인 처리 요청
        MavenMember loginMember = new MavenMember(id, pw);
        MemberDAO dao = new MemberDAO();

        // 로그인 기능 실행
        MavenMember sMember = dao.login(loginMember);

        // 4. 결과 처리
        if (sMember != null) {
            // 로그인 성공 시, 세션에 sMember 저장
            HttpSession session = request.getSession();
            session.setAttribute("sMember", sMember);

            // 로그인 성공 후 dashboard.jsp로 이동
            response.sendRedirect("dashboard.jsp");
        } else {
            // 로그인 실패 시, main.jsp로 리디렉션
            response.sendRedirect("main.jsp");
        }
    }
}