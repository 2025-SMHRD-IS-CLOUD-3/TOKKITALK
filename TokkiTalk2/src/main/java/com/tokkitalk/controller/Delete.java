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

@WebServlet("/Delete")
public class Delete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        
        // 1. 클라이언트에서 보낸 사용자 ID 받기
        String id = request.getParameter("id");
        
        // --- ★가장 중요한 디버깅 코드★ ---
        // 이 코드가 Tomcat 콘솔에 'null'을 출력하면, ID가 서버로 넘어오지 않았다는 의미입니다.
        System.out.println("서버가 받은 회원 탈퇴 요청 ID: " + id);
        // ----------------------------------------
        
        if (id == null || id.trim().isEmpty()) {
            out.print("{\"success\": false, \"message\": \"로그인 정보가 유효하지 않습니다.\"}");
            return;
        }
        
        MemberDAO dao = new MemberDAO();
        
        try {
            // 2. MemberDAO의 deleteMember 메서드 호출
            int row = dao.deleteMember(id);
            
            if (row > 0) {
                // 3-1. 삭제 성공
                HttpSession session = request.getSession();
                session.invalidate(); // 세션 무효화
                out.print("{\"success\": true}");
            } else {
                // 3-2. 삭제 실패 (회원 정보가 없거나 DB 오류)
                out.print("{\"success\": false, \"message\": \"회원 탈퇴 실패\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"서버 오류\"}");
        }
    }
}