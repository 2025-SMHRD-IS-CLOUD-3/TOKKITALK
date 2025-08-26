package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.tokkitalk.analysis.store.AnalysisDAO;
import com.tokkitalk.model.MevenMember;

@WebServlet("/saveHistory")
public class SaveHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[서버] /saveHistory 요청 받음");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("member") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"로그인이 필요합니다.\"}");
                return;
            }
            
            MevenMember member = (MevenMember) session.getAttribute("member");
            String userIdStr = member.getUser_id();
            System.out.println("DEBUG: userId (String) = " + userIdStr);
            
            SaveHistoryRequest saveRequest = gson.fromJson(request.getReader(), SaveHistoryRequest.class);
            
            // 어시스턴트 메시지를 빌드하는 로직 (이전 코드를 기반으로 재구성)
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("=== 분석 결과 ===\n");
            
            if (saveRequest.result != null) {
                if (saveRequest.result.surface_meaning != null) {
                    messageBuilder.append("📝 표면적 의미:\n");
                    messageBuilder.append(saveRequest.result.surface_meaning.one_line).append("\n\n");
                }
                if (saveRequest.result.hidden_meaning != null) {
                    messageBuilder.append("🔍 숨은 의도:\n");
                    messageBuilder.append(saveRequest.result.hidden_meaning.one_line).append("\n\n");
                }
                if (saveRequest.result.emotion != null) {
                    messageBuilder.append("😊 감정 상태:\n");
                    messageBuilder.append(saveRequest.result.emotion.label).append("\n\n");
                }
                if (saveRequest.result.advice != null && !saveRequest.result.advice.isEmpty()) {
                    messageBuilder.append("💡 제안:\n");
                    for (AdviceData advice : saveRequest.result.advice) {
                        messageBuilder.append("- ").append(advice.style).append(": ").append(advice.text).append("\n");
                    }
                }
            } else {
                 messageBuilder.append("분석 결과가 없습니다.\n");
            }
            
            String userMessage = (saveRequest.input_text != null && !saveRequest.input_text.trim().isEmpty()) ? saveRequest.input_text.trim() : "[내용 없음]";
            String assistantMessage = messageBuilder.toString().trim();
            
            // DB 저장 로직: 사용자 메시지와 어시스턴트 메시지를 별도로 저장
            try {
                // 사용자 메시지 저장
                analysisDAO.saveToChatHistory(userIdStr, "user", userMessage);
                
                // 어시스턴트 메시지 저장
                analysisDAO.saveToChatHistory(userIdStr, "assistant", assistantMessage);
                
                System.out.println("DB 저장 성공!");
                out.println("{\"success\": true, \"message\": \"분석 결과가 히스토리에 저장되었습니다.\"}");
            } catch (Exception dbError) {
                System.out.println("❌ DB 저장 실패: " + dbError.getMessage());
                dbError.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"저장 중 오류가 발생했습니다: " + dbError.getMessage() + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[서버] 예외 발생: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"저장 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // 내부 클래스들은 그대로 유지
    public static class SaveHistoryRequest {
        public String input_text;
        public AnalysisResultData result;
        public String input_image_base64;
    }
    
    public static class AnalysisResultData {
        public SubResultData surface_meaning;
        public SubResultData hidden_meaning;
        public EmotionData emotion;
        public List<AdviceData> advice;
    }
    
    public static class SubResultData {
        public String one_line;
        public String detailed;
    }
    
    public static class EmotionData {
        public String label;
        public double valence;
    }
    
    public static class AdviceData {
        public String style;
        public String text;
    }
}