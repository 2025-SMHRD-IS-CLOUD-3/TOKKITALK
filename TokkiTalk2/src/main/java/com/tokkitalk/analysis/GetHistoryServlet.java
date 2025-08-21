package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.tokkitalk.analysis.store.AnalysisDAO;

@WebServlet("/getHistory")
public class GetHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 세션에서 사용자 ID 가져오기
            HttpSession session = request.getSession();
            Long sessionUserId = (Long) session.getAttribute("userId");
            
            // 테스트용: 로그인 안 되어 있으면 임시 userId 사용
            final Long userId;
            if (sessionUserId == null) {
                userId = 1L;
                System.out.println("테스트용 userId 사용: " + userId);
            } else {
                userId = sessionUserId;
            }
            
            System.out.println("히스토리 조회 시작 - User ID: " + userId);
            
            // DB에서 히스토리 가져오기
            AnalysisDAO dao = new AnalysisDAO();
            List<HistoryItem> historyItems = dao.getChatHistory(userId);
            
            System.out.println("DB에서 가져온 히스토리 개수: " + historyItems.size());
            
            // 최신순 정렬
            historyItems.sort(Comparator.comparing(HistoryItem::getCreatedAt).reversed());
            
            // JSON 응답 생성
            String jsonResponse = convertToJson(historyItems);
            System.out.println("응답 전송: " + historyItems.size() + "개 아이템");
            out.println(jsonResponse);
            
        } catch (Exception e) {
            System.err.println("GetHistoryServlet 오류: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"히스토리 조회 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // JSON 변환 메서드
    private String convertToJson(List<HistoryItem> items) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < items.size(); i++) {
            HistoryItem item = items.get(i);
            json.append("{");
            json.append("\"id\":\"").append(escapeJson(item.getId())).append("\",");
            json.append("\"date\":\"").append(escapeJson(item.getDate())).append("\",");
            json.append("\"time\":\"").append(escapeJson(item.getTime())).append("\",");
            json.append("\"role\":\"").append(escapeJson(item.getRole())).append("\",");
            json.append("\"content\":\"").append(escapeJson(item.getContent())).append("\"");
            json.append("}");
            
            if (i < items.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    // JSON 이스케이프
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // 히스토리 아이템 클래스
    public static class HistoryItem {
        private Long id;
        private Long userId;
        private String role;
        private String content;
        private String createdAt;
        private String date;
        private String time;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { 
            this.createdAt = createdAt;
            // createdAt에서 date와 time 추출
            if (createdAt != null && createdAt.length() >= 19) {
                this.date = createdAt.substring(0, 10); // YYYY-MM-DD
                this.time = createdAt.substring(11, 19); // HH:MM:SS
            }
        }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }
}
