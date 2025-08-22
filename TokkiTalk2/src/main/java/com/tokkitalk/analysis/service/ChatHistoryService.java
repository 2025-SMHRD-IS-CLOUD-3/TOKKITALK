package com.tokkitalk.analysis.service;

import java.util.List;

import com.tokkitalk.analysis.GetHistoryServlet.HistoryItem;
import com.tokkitalk.analysis.store.AnalysisDAO;

public class ChatHistoryService {
    
    private final AnalysisDAO analysisDAO;
    
    public ChatHistoryService() {
        this.analysisDAO = new AnalysisDAO();
    }
    
    /**
     * 사용자 메시지를 히스토리에 저장
     * @param userId 사용자 ID
     * @param role 메시지 역할 (user/assistant)
     * @param message 메시지 내용
     */
    public void saveMessage(String userId, String role, String message) {
        try {
            Long userIdLong = Long.parseLong(userId);
            analysisDAO.saveToChatHistory(userIdLong, role, message);
            System.out.println("번역 히스토리 저장 성공 - User: " + userId + ", Role: " + role);
        } catch (NumberFormatException e) {
            System.err.println("사용자 ID 변환 오류: " + userId);
            throw new RuntimeException("유효하지 않은 사용자 ID입니다.", e);
        } catch (Exception e) {
            System.err.println("번역 히스토리 저장 실패: " + e.getMessage());
            throw new RuntimeException("히스토리 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 특정 사용자의 번역 히스토리 조회
     * @param userId 사용자 ID
     * @return 히스토리 목록
     */
    public List<HistoryItem> getUserHistory(String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            List<HistoryItem> history = analysisDAO.getChatHistory(userIdLong);
            System.out.println("번역 히스토리 조회 성공 - User: " + userId + ", 개수: " + history.size());
            return history;
        } catch (NumberFormatException e) {
            System.err.println("사용자 ID 변환 오류: " + userId);
            throw new RuntimeException("유효하지 않은 사용자 ID입니다.", e);
        } catch (Exception e) {
            System.err.println("번역 히스토리 조회 실패: " + e.getMessage());
            throw new RuntimeException("히스토리 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 번역 결과를 히스토리에 저장 (사용자 입력 + 번역 결과)
     * @param userId 사용자 ID
     * @param userInput 사용자 입력 메시지
     * @param translationResult 번역 결과
     */
    public void saveTranslationHistory(String userId, String userInput, String translationResult) {
        // 사용자 입력 저장
        saveMessage(userId, "user", userInput);
        
        // 번역 결과 저장
        saveMessage(userId, "assistant", translationResult);
        
        System.out.println("번역 히스토리 저장 완료 - User: " + userId);
    }
}
