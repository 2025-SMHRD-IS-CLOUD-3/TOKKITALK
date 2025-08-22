package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.tokkitalk.analysis.service.ChatHistoryService;

@WebServlet("/saveTranslationHistory")
public class SaveTranslationHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ChatHistoryService chatHistoryService;
    private final Gson gson;
    
    public SaveTranslationHistoryServlet() {
        this.chatHistoryService = new ChatHistoryService();
        this.gson = new Gson();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 세션에서 사용자 ID 가져오기
            HttpSession session = request.getSession();
            String sessionUserId = (String) session.getAttribute("userId");
            
            // 테스트용: 로그인 안 되어 있으면 임시 userId 사용
            String userId;
            if (sessionUserId == null) {
                userId = "1";
                System.out.println("테스트용 userId 사용: " + userId);
            } else {
                userId = sessionUserId;
            }
            
            // 요청 본문 읽기
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            String requestBody = sb.toString();
            System.out.println("번역 히스토리 저장 요청: " + requestBody);
            
            // JSON 파싱
            TranslationRequest translationRequest = gson.fromJson(requestBody, TranslationRequest.class);
            
            if (translationRequest == null || translationRequest.userInput == null || translationRequest.translationResult == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"필수 데이터가 누락되었습니다.\"}");
                return;
            }
            
            // 번역 히스토리 저장
            chatHistoryService.saveTranslationHistory(userId, translationRequest.userInput, translationRequest.translationResult);
            
            // 성공 응답
            out.println("{\"success\": true, \"message\": \"번역 히스토리가 저장되었습니다.\"}");
            
        } catch (Exception e) {
            System.err.println("번역 히스토리 저장 오류: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"번역 히스토리 저장 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // 번역 요청 DTO
    public static class TranslationRequest {
        public String userInput;
        public String translationResult;
    }
}
