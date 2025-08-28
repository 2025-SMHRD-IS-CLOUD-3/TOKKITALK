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
 // 기존: 텍스트만 저장할 때 사용
    public void saveMessage(String userId, String role, String message) {
        try {
            // 🚨 userId를 String 타입 그대로 사용하도록 변경
            analysisDAO.saveToChatHistory(userId, role, message);
            System.out.println("번역 히스토리 저장 성공 - User: " + userId + ", Role: " + role);
        } catch (Exception e) {
            System.err.println("번역 히스토리 저장 실패: " + e.getMessage());
            throw new RuntimeException("히스토리 저장 중 오류가 발생했습니다.", e);
        }
    }
 // 추가: 이미지도 함께 저장할 때 사용
    public void saveMessage(String userId, String role, String message, String imageBase64) {
        try {
            analysisDAO.saveToChatHistory(userId, role, message, imageBase64);
            System.out.println("히스토리 저장 (이미지 포함) 성공 - User: " + userId + ", Role: " + role);
        } catch (Exception e) {
            System.err.println("히스토리 저장 (이미지 포함) 실패: " + e.getMessage());
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
            // 🚨 userId를 String 타입 그대로 사용하도록 변경
            List<HistoryItem> history = analysisDAO.getChatHistory(userId);
            System.out.println("번역 히스토리 조회 성공 - User: " + userId + ", 개수: " + history.size());
            return history;
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