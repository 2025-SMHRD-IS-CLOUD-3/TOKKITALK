package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/testDB")
public class TestDBServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>DB 연결 테스트</title></head><body>");
        out.println("<h1>🔍 Oracle DB 연결 테스트</h1>");
        
        // DB 연결 정보
        String driver = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:thin:@project-db-campus.smhrd.com:1524:xe";
        String username = "campus_24IS_CLOUD3_p2_1";
        String password = "smhrd1";
        
        out.println("<h3>DB 연결 정보:</h3>");
        out.println("<ul>");
        out.println("<li><strong>Driver:</strong> " + driver + "</li>");
        out.println("<li><strong>URL:</strong> " + url + "</li>");
        out.println("<li><strong>Username:</strong> " + username + "</li>");
        out.println("<li><strong>Password:</strong> " + password + "</li>");
        out.println("</ul>");
        
        // 1. JDBC 드라이버 로드 테스트
        out.println("<h3>1. JDBC 드라이버 로드 테스트:</h3>");
        try {
            Class.forName(driver);
            out.println("<p style='color: green;'>✅ JDBC 드라이버 로드 성공!</p>");
        } catch (ClassNotFoundException e) {
            out.println("<p style='color: red;'>❌ JDBC 드라이버 로드 실패: " + e.getMessage() + "</p>");
            out.println("<p><strong>해결방법:</strong> Oracle JDBC 드라이버가 클래스패스에 있는지 확인하세요.</p>");
            out.println("</body></html>");
            return;
        }
        
        // 2. DB 연결 테스트
        out.println("<h3>2. DB 연결 테스트:</h3>");
        Connection conn = null;
        try {
            out.println("<p>DB에 연결 시도 중...</p>");
            conn = DriverManager.getConnection(url, username, password);
            out.println("<p style='color: green;'>✅ DB 연결 성공!</p>");
            
            // 3. 테이블 존재 확인
            out.println("<h3>3. CHAT_HISTORY 테이블 확인:</h3>");
            try {
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CHAT_HISTORY");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    out.println("<p style='color: green;'>✅ CHAT_HISTORY 테이블 접근 성공! (레코드 수: " + count + ")</p>");
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                out.println("<p style='color: red;'>❌ CHAT_HISTORY 테이블 접근 실패: " + e.getMessage() + "</p>");
                out.println("<p><strong>가능한 원인:</strong></p>");
                out.println("<ul>");
                out.println("<li>테이블이 존재하지 않음</li>");
                out.println("<li>사용자 권한 부족</li>");
                out.println("<li>테이블명 오타</li>");
                out.println("</ul>");
            }
            
        } catch (SQLException e) {
            out.println("<p style='color: red;'>❌ DB 연결 실패: " + e.getMessage() + "</p>");
            out.println("<p><strong>가능한 원인:</strong></p>");
            out.println("<ul>");
            out.println("<li>네트워크 연결 문제</li>");
            out.println("<li>DB 서버가 실행되지 않음</li>");
            out.println("<li>포트 번호 오류 (1524)</li>");
            out.println("<li>사용자명/비밀번호 오류</li>");
            out.println("<li>DB 서비스명 오류 (xe)</li>");
            out.println("</ul>");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    out.println("<p style='color: blue;'>🔒 DB 연결 종료됨</p>");
                } catch (SQLException e) {
                    out.println("<p style='color: orange;'>⚠️ DB 연결 종료 중 오류: " + e.getMessage() + "</p>");
                }
            }
        }
        
        // 4. 해결 방법 제안
        out.println("<h3>4. 문제 해결 방법:</h3>");
        out.println("<ol>");
        out.println("<li><strong>네트워크 확인:</strong> project-db-campus.smhrd.com:1524에 ping 테스트</li>");
        out.println("<li><strong>DB 서버 상태:</strong> DB 서버가 실행 중인지 확인</li>");
        out.println("<li><strong>방화벽:</strong> 1524 포트가 열려있는지 확인</li>");
        out.println("<li><strong>JDBC 드라이버:</strong> Oracle JDBC 드라이버가 프로젝트에 포함되어 있는지 확인</li>");
        out.println("</ol>");
        
        out.println("<hr>");
        out.println("<p><a href='test-history.html'>← 히스토리 테스트로 돌아가기</a></p>");
        out.println("</body></html>");
    }
}
