package com.tokkitalk.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tokkitalk.model.MavenMember;
import com.tokkitalk.model.MemberDAO;

@WebServlet("/Join")
public class Join extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. join.html에서 회원가입 정보를 받아오기 전에 인코딩 설정
        request.setCharacterEncoding("UTF-8");

        // 2. main.jsp에서 데이터를 받아오기
        String id = request.getParameter("id");        // 아이디
        String pw = request.getParameter("pw");        // 비밀번호
        String name = request.getParameter("name");    // 이름
        String gender = request.getParameter("gender"); // 성별
        String birthDate = request.getParameter("birthDate"); // 생년월일

        // 3. 받은 데이터로 MavenMember 객체 생성
        MavenMember joinMember = new MavenMember(id, pw, name, gender, birthDate);

        // 4. MemberDAO 객체 생성 후 회원가입 처리
        MemberDAO memberDAO = new MemberDAO();
        boolean result = memberDAO.insertMember(joinMember); // 여기에서 boolean 값을 받음

        // 5. 결과 값 처리
        if (result) {  // boolean 타입이므로 result를 바로 사용
            // 회원가입 성공 시, join_success.jsp로 포워드
            request.setAttribute("id", id);
            RequestDispatcher rd = request.getRequestDispatcher("join_success.jsp");
            rd.forward(request, response);
        } else {
            // 실패 시, main.jsp로 리디렉션
            response.sendRedirect("main.jsp");
        }
    }
}