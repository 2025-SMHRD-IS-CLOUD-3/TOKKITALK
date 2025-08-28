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
            // 세션에서 사용자 정보 가져오기
            HttpSession session = request.getSession();
            com.tokkitalk.model.MevenMember member = (com.tokkitalk.model.MevenMember) session.getAttribute("member");
            
            // 로그인 안 되어 있으면 에러 처리
            if (member == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"로그인이 필요합니다.\"}");
                return;
            }
            
            final String userId = member.getUser_id();
            System.out.println("히스토리 조회 시작 - 로그인된 사용자 ID: " + userId);
            
            // DB에서 히스토리 가져오기
            AnalysisDAO dao = new AnalysisDAO();
            List<HistoryItem> historyItems = dao.getChatHistory(userId);
            
            System.out.println("DB에서 가져온 히스토리 개수: " + historyItems.size());
            
            // 최신순 정렬 (null 안전하게 처리)
            historyItems.sort((a, b) -> {
                String dateA = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                String dateB = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                return dateB.compareTo(dateA); // 최신순 정렬
            });
            
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
            json.append("\"chat_id\":\"").append(escapeJson(item.getId() != null ? item.getId().toString() : "")).append("\",");
            json.append("\"role\":\"").append(escapeJson(item.getRole())).append("\",");
            json.append("\"message_text\":\"").append(escapeJson(item.getContent())).append("\",");
            json.append("\"created_at\":\"").append(escapeJson(item.getCreatedAt())).append("\",");
            json.append("\"image_base64\":\"").append(escapeJson(item.getImageBase64() != null ? item.getImageBase64() : ""))
            .append("\",");
            json.append("\"image_url\":null");
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
        private String userId;
        private String role;
        private String content;
        private String createdAt;
        private String date;
        private String time;
        private String imageBase64;
        
        // Getters and Setters
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { 
            this.createdAt = createdAt;
            // createdAt에서 date와 time 추출
            if (createdAt != null && createdAt.length() >= 19) {
                try {
                    this.date = createdAt.substring(0, 10); // YYYY-MM-DD
                    this.time = createdAt.substring(11, 19); // HH:MM:SS
                } catch (Exception e) {
                    System.err.println("날짜 파싱 오류: " + createdAt + " - " + e.getMessage());
                    this.date = "";
                    this.time = "";
                }
            } else {
                this.date = "";
                this.time = "";
            }
        }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }
}
