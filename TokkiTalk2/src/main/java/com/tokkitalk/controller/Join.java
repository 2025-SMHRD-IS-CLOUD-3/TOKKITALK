package com.tokkitalk.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tokkitalk.model.MemberDAO;
import com.tokkitalk.model.MevenMember;

@WebServlet("/join")
public class Join extends HttpServlet {
   private static final long serialVersionUID = 1L;

   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // 1. 인코딩
      request.setCharacterEncoding("UTF-8");
      
      // 2. 파라미터 수집
      String user_id = request.getParameter("id");
      String user_pw = request.getParameter("pw");
      String user_name = request.getParameter("name");
      String gender = request.getParameter("gender");
      String user_date_str = request.getParameter("user_date");
      
      // 3. 날짜 변환
      Date user_date = null;
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         user_date = sdf.parse(user_date_str);
      } catch (Exception e) {
         user_date = new Date(); // 현재 날짜로 설정
      }
      
      // 4. Member 객체 생성
      MevenMember member = new MevenMember(user_id, user_pw, user_name, gender, user_date);
      
      // 5. DAO 객체 생성
      MemberDAO dao = new MemberDAO();
      
      // 6. 회원가입 실행
      int cnt = dao.join(member);
      
      // 7. 결과에 따른 페이지 이동
      if (cnt > 0) {
         // 회원가입 성공
         response.sendRedirect("main.jsp?msg=join_success");
      } else {
         // 회원가입 실패
         response.sendRedirect("main.jsp?msg=join_fail");
      }
   }
}