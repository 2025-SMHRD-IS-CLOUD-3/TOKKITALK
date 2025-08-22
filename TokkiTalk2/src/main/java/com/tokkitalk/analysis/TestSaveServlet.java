package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tokkitalk.analysis.store.AnalysisDAO;

@WebServlet("/testSave")
public class TestSaveServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>DB 저장 테스트</title></head><body>");
        out.println("<h1>🔍 DB 저장 테스트</h1>");
        
        try {
            // 1. AnalysisDAO 인스턴스 생성 테스트
            out.println("<h3>1. AnalysisDAO 인스턴스 생성:</h3>");
            AnalysisDAO dao = new AnalysisDAO();
            out.println("<p style='color: green;'>✅ AnalysisDAO 생성 성공!</p>");
            
            // 2. CHAT_HISTORY INSERT 테스트
            out.println("<h3>2. CHAT_HISTORY INSERT 테스트:</h3>");
            try {
                dao.saveToChatHistory("1", "test", "테스트 메시지 - " + System.currentTimeMillis());
                out.println("<p style='color: green;'>✅ DB INSERT 성공!</p>");
            } catch (Exception e) {
                out.println("<p style='color: red;'>❌ DB INSERT 실패: " + e.getMessage() + "</p>");
                out.println("<p><strong>스택 트레이스:</strong></p>");
                out.println("<pre style='background: #f5f5f5; padding: 10px; border-radius: 5px;'>");
                e.printStackTrace(out);
                out.println("</pre>");
            }
            
        } catch (Exception e) {
            out.println("<p style='color: red;'>❌ 테스트 오류: " + e.getMessage() + "</p>");
            out.println("<p><strong>스택 트레이스:</strong></p>");
            out.println("<pre style='background: #f5f5f5; padding: 10px; border-radius: 5px;'>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
        
        out.println("<hr>");
        out.println("<p><a href='Question.html'>← 분석 페이지로 돌아가기</a></p>");
        out.println("</body></html>");
    }
}
