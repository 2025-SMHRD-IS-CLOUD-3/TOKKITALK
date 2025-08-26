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
import com.tokkitalk.analysis.store.AnalysisRecord;
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
            
            // JSON 요청을 SaveHistoryRequest 객체로 파싱
            SaveHistoryRequest saveRequest = gson.fromJson(request.getReader(), SaveHistoryRequest.class);
            
            // 🚨 수정된 유효성 검사 로직
            String inputText = saveRequest.input_text;
            String inputImageBase64 = saveRequest.input_image_base64; // data 대신 saveRequest 객체 사용

            // 텍스트와 이미지 둘 다 null이거나 비어있는 경우에 에러 반환
            if (inputText == null || inputText.trim().isEmpty()) {
                inputText = "[내용 없음]";
            }
            if (inputImageBase64 == null || inputImageBase64.trim().isEmpty()) {
                inputImageBase64 = null; // 이미지 없으면 null로 두기
            }

         // AnalysisRecord 객체에 데이터 설정
            AnalysisRecord record = new AnalysisRecord();
            record.setUserId(userIdStr);
            record.setText(inputText); // null일 경우 "[내용 없음]" 들어감

            System.out.println("[DEBUG] record.getText(): " + record.getText());

            if (inputImageBase64 != null) {
                record.setInputType("IMAGE");
                String pureBase64 = inputImageBase64.substring(inputImageBase64.indexOf(",") + 1);
                record.setImageBase64(pureBase64);
            } else {
                record.setInputType("TEXT");
                record.setImageUrl(null);
            }
            
            String analysisResultJson = gson.toJson(saveRequest.result);
            record.setAnalysisResult(analysisResultJson);
            
            System.out.println("=== DB에 저장될 AnalysisRecord 객체 정보 ===");
            System.out.println(record.toString());
            
            boolean success = analysisDAO.insertAnalysis(record);
            
            if (success) {
                System.out.println("DB 저장 성공!");
                
                // 🚨 CHAT_HISTORY에 저장하는 로직
                analysisDAO.saveToChatHistory(userIdStr, "user", record.getText());
                
                out.println("{\"success\": true, \"message\": \"분석 결과가 히스토리에 저장되었습니다.\"}");
            } else {
                throw new Exception("DAO에서 DB 저장 실패를 반환했습니다.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[서버] 예외 발생: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"저장 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // SaveHistoryRequest, AnalysisResultData 등 내부 클래스들은 변경 없음
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