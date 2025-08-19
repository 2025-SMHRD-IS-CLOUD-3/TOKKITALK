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

@WebServlet("/login")
public class Login extends HttpServlet {
   private static final long serialVersionUID = 1L;

   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // 1. 인코딩
      request.setCharacterEncoding("UTF-8");
      
      // 2. 파라미터 수집
      String user_id = request.getParameter("id");
      String user_pw = request.getParameter("pw");
      String ajax = request.getParameter("ajax");
      
      System.out.println("=== 로그인 시도 ===");
      System.out.println("입력된 ID: " + user_id);
      System.out.println("입력된 PW: " + user_pw);
      
      // 3. Member 객체 생성
      MevenMember member = new MevenMember(user_id, user_pw, null, null, null);
      
      // 4. DAO 객체 생성
      MemberDAO dao = new MemberDAO();
      
      // 5. 로그인 실행
      MevenMember result = dao.login(member);
      
      System.out.println("로그인 결과: " + (result != null ? "성공" : "실패"));
      if (result != null) {
         System.out.println("조회된 사용자 정보:");
         System.out.println("- ID: " + result.getUser_id());
         System.out.println("- 이름: " + result.getUser_name());
         System.out.println("- 성별: " + result.getGender());
         System.out.println("- 가입일: " + result.getUser_date());
      }
      
      // 6. 결과에 따른 응답 처리 (AJAX vs Redirect)
      if ("1".equals(ajax)) {
         response.setCharacterEncoding("UTF-8");
         response.setContentType("application/json; charset=UTF-8");
         try (PrintWriter out = response.getWriter()) {
            if (result != null) {
               HttpSession session = request.getSession();
               session.setAttribute("member", result);
               out.write("{\"success\":true,\"userId\":\"" + result.getUser_id() + "\",\"userName\":\"" + (result.getUser_name() != null ? result.getUser_name() : result.getUser_id()) + "\"}");
            } else {
               out.write("{\"success\":false}");
            }
         }
      } else {
         if (result != null) {
            // 로그인 성공
            HttpSession session = request.getSession();
            session.setAttribute("member", result);
            System.out.println("세션에 사용자 정보 저장 완료");
            response.sendRedirect("main.html?username=" + result.getUser_id());
         } else {
            // 로그인 실패
            System.out.println("로그인 실패 - main.html로 리다이렉트");
            response.sendRedirect("main.html?msg=login_fail");
         }
      }
   }
}
