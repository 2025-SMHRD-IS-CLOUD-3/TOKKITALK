package com.tokkitalk.controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.tokkitalk.model.MemberDAO;
import com.tokkitalk.model.MevenMember;

@WebServlet("/Update")
public class Update extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        HttpSession session = request.getSession();
        MevenMember existingMember = (MevenMember) session.getAttribute("member");

        if (existingMember == null) {
            out.print(gson.toJson(new ApiResponse(false, "로그인이 필요합니다.")));
            return;
        }

        String user_id = request.getParameter("id");
        String current_pw = request.getParameter("currentPw");
        String new_pw = request.getParameter("newPw");
        
        System.out.println("회원 ID: " + user_id);
        System.out.println("현재 PW: " + current_pw);
        System.out.println("새 PW: " + new_pw);
        
        // 현재 비밀번호 일치 여부 확인
        if (!current_pw.equals(existingMember.getUser_pw())) {
            out.print(gson.toJson(new ApiResponse(false, "현재 비밀번호가 일치하지 않습니다.")));
            return;
        }
        
        // 업데이트할 회원 정보 객체 생성
        MevenMember updatedMember = new MevenMember();
        updatedMember.setUser_id(user_id);
        updatedMember.setUser_pw(new_pw);

        // DAO를 통해 데이터베이스 업데이트 실행
        MemberDAO dao = new MemberDAO();
        int cnt = dao.update(updatedMember);
        
        // 업데이트 결과 판별 및 세션 갱신
        if (cnt > 0) {
            existingMember.setUser_pw(new_pw);
            out.print(gson.toJson(new ApiResponse(true, "비밀번호가 성공적으로 변경되었습니다.")));
        } else {
            out.print(gson.toJson(new ApiResponse(false, "비밀번호 업데이트에 실패했습니다.")));
        }
    }

    class ApiResponse {
        boolean success;
        String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}