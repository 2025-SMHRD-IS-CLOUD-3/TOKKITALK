package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.store.AnalysisDAO;

@WebServlet("/saveHistory")
public class SaveHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 세션에서 사용자 정보 가져오기
            HttpSession session = request.getSession();
            com.tokkitalk.model.MevenMember member = (com.tokkitalk.model.MevenMember) session.getAttribute("member");
            
            // 디버깅: 세션 정보 출력
            System.out.println("=== 세션 디버깅 ===");
            System.out.println("세션 ID: " + session.getId());
            System.out.println("세션 생성 시간: " + session.getCreationTime());
            System.out.println("세션 마지막 접근 시간: " + session.getLastAccessedTime());
            System.out.println("세션에서 가져온 member: " + member);
            
            // 세션의 모든 속성 출력
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            System.out.println("세션의 모든 속성:");
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object value = session.getAttribute(name);
                System.out.println("  " + name + " = " + value);
            }
            System.out.println("=== 세션 디버깅 끝 ===");
            
            // 로그인 안 되어 있으면 에러 처리
            if (member == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"로그인이 필요합니다.\"}");
                return;
            }
            
            String userId = member.getUser_id();
            System.out.println("로그인된 사용자 ID: " + userId);
            
            // 요청 데이터 파싱
            SaveHistoryRequest saveRequest = gson.fromJson(
                request.getReader(), SaveHistoryRequest.class);
            
            // 분석 결과를 JSON 형태로 저장할 메시지 구성
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("=== 분석 입력 ===\n");
            messageBuilder.append(saveRequest.input_text).append("\n\n");
            
            if (saveRequest.input_image_base64 != null) {
                messageBuilder.append("이미지 첨부됨\n\n");
            }
            
            messageBuilder.append("=== 분석 결과 ===\n");
            
            if (saveRequest.result.surface_meaning != null) {
                messageBuilder.append("📝 표면적 의미:\n");
                messageBuilder.append(saveRequest.result.surface_meaning).append("\n\n");
            }
            
            if (saveRequest.result.hidden_meaning != null) {
                messageBuilder.append("🔍 숨은 의도:\n");
                messageBuilder.append(saveRequest.result.hidden_meaning).append("\n\n");
            }
            
            if (saveRequest.result.emotion != null) {
                messageBuilder.append("😊 감정 상태:\n");
                messageBuilder.append(saveRequest.result.emotion).append("\n\n");
            }
            
            if (saveRequest.result.advice != null) {
                messageBuilder.append("💡 제안:\n");
                messageBuilder.append(saveRequest.result.advice).append("\n\n");
            }
            
            // CHAT_HISTORY 테이블에 저장 (임시로 콘솔 출력)
            System.out.println("=== 저장할 데이터 ===");
            System.out.println("User ID: " + userId);
            System.out.println("User Message: " + saveRequest.input_text);
            System.out.println("Assistant Message: " + messageBuilder.toString());
            
            // 파일로 저장
            try {
                saveToFile(userId, "user", saveRequest.input_text);
                saveToFile(userId, "assistant", messageBuilder.toString());
                System.out.println("파일 저장 성공!");
            } catch (Exception fileError) {
                System.out.println("파일 저장 실패: " + fileError.getMessage());
                fileError.printStackTrace();
            }
            
            // DB에도 저장 시도
            try {
                System.out.println("=== DB 저장 시도 ===");
                saveToChatHistory(userId, "user", saveRequest.input_text);
                saveToChatHistory(userId, "assistant", messageBuilder.toString());
                System.out.println("DB 저장 성공!");
            } catch (Exception dbError) {
                System.out.println("❌ DB 저장 실패: " + dbError.getMessage());
                System.out.println("=== 상세 에러 정보 ===");
                dbError.printStackTrace();
                System.out.println("=== 에러 정보 끝 ===");
                System.out.println("파일 저장은 성공했으므로 계속 진행합니다.");
            }
            
            // 성공 응답
            out.println("{\"success\": true, \"message\": \"분석 결과가 히스토리에 저장되었습니다.\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"저장 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    private void saveToFile(String userId, String role, String message) {
        try {
            // 절대 경로로 저장할 디렉토리 생성
            String projectRoot = System.getProperty("user.dir");
            java.io.File dir = new java.io.File(projectRoot + "/history_logs");
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            // 파일명 생성 (날짜_시간_userId_role.txt)
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/history_logs/%s_%s_%s.txt", projectRoot, timestamp, userId, role);
            
            // 파일에 저장
            java.io.File file = new java.io.File(filename);
            java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8");
            writer.println("=== " + role.toUpperCase() + " MESSAGE ===");
            writer.println("User ID: " + userId);
            writer.println("Timestamp: " + java.time.LocalDateTime.now());
            writer.println("Message:");
            writer.println(message);
            writer.close();
            
            System.out.println("파일 저장 완료: " + filename);
            System.out.println("프로젝트 루트: " + projectRoot);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 저장 중 오류: " + e.getMessage());
        }
    }
    
    private void saveToChatHistory(String userId, String role, String message) {
        try {
            analysisDAO.saveToChatHistory(userId, role, message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("히스토리 저장 중 오류: " + e.getMessage());
        }
    }
    
    private String saveImageFromBase64(String base64Data, String analysisId) {
        // Base64 이미지를 파일로 저장하는 로직
        // 실제 구현에서는 파일 시스템에 저장하고 URL 반환
        try {
            // Base64 데이터에서 실제 이미지 데이터 추출
            String imageData = base64Data.replaceFirst("data:image/[^;]*;base64,", "");
            
            // 파일명 생성
            String fileName = analysisId + "_" + System.currentTimeMillis() + ".jpg";
            
            // 실제 구현에서는 파일 시스템에 저장
            // Files.write(Paths.get("uploads/" + fileName), Base64.getDecoder().decode(imageData));
            
            return "/uploads/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 요청 데이터 클래스
    public static class SaveHistoryRequest {
        public String input_text;
        public AnalysisResultData result;
        public String input_image_base64;
    }
    
    public static class AnalysisResultData {
        public String surface_meaning;
        public String hidden_meaning;
        public String emotion;
        public String advice;
    }
}
