package com.tokkitalk.analysis.store;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.FeedbackRequest;
import com.tokkitalk.analysis.GetHistoryServlet;
import com.tokkitalk.analysis.store.AnalysisRecord;

public class AnalysisDAO {

    private static final String MAPPER_NS = "com.tokkitalk.db.AnalysisMapper";
    private static final Gson gson = new Gson();

    private static final SqlSessionFactory sqlSessionFactory = buildSessionFactory();

    private static SqlSessionFactory buildSessionFactory() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-db.xml");
            return new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MyBatis configuration", e);
        }
    }

    // 🚨 1. saveResult 메서드: Long userId -> String userId로 변경
    public void saveResult(AnalysisResult result, String userId) {
        AnalysisRecord rec = new AnalysisRecord();
        
        // String to long 변환 (이 부분은 analysis_id에 대한 것이므로 그대로 둡니다.)
        long analysisIdLong = Long.parseLong(result.analysis_id);
        rec.setAnalysisId(analysisIdLong);
        
        rec.setInputType("text");
        rec.setText(result.surface_meaning != null ? result.surface_meaning.one_line : null);
        rec.setImageUrl(null);
        rec.setTone((result.response_suggestion != null && result.response_suggestion.tone != null)
                ? result.response_suggestion.tone : "");
        rec.setAnalysisResult(gson.toJson(result));
        rec.setSuggestions(result.response_suggestion != null ? gson.toJson(result.response_suggestion) : null);
        rec.setUserId(userId); // 🚨 userId를 String으로 설정

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertAnalysis", rec);
        }
    }

    public AnalysisResult findById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AnalysisRecord row = session.selectOne(MAPPER_NS + ".selectAnalysis", analysisId);
            if (row == null)
                return null;
            if (row.getAnalysisResult() != null) {
                return gson.fromJson(row.getAnalysisResult(), AnalysisResult.class);
            }
            AnalysisResult result = new AnalysisResult();
            result.analysis_id = analysisId;
            return result;
        }
    }

    // 🚨 2. selectByUser 메서드: long userId -> String userId로 변경
    public java.util.List<AnalysisRecord> selectByUser(String userId, int offset, int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            java.util.Map<String, Object> p = new java.util.HashMap<>();
            p.put("userId", userId);
            p.put("offset", offset);
            p.put("limit", limit);
            return session.selectList(MAPPER_NS + ".selectByUser", p);
        }
    }

    public void updateSuggestion(AnalysisResult updated) {
        if (updated == null || updated.response_suggestion == null) {
            return;
        }
        saveResult(updated, "0"); // 🚨 userId를 String으로 전달
    }

    public void delete(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete(MAPPER_NS + ".deleteAnalysis", analysisId);
        }
    }

    public void saveFeedback(FeedbackRequest feedbackRequest) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> params = new HashMap<>();
            params.put("analysis_id", feedbackRequest.analysis_id);
            int rating = feedbackRequest.like != null && feedbackRequest.like ? 1 : 0;
            params.put("feedback_rating", rating);
            params.put("feedback_comment", feedbackRequest.partner_reaction);
            session.update(MAPPER_NS + ".updateFeedback", params);
        }
    }

    // 🚨 3. saveToChatHistory 메서드: 매개변수 타입을 String으로 명확하게 변경
    public void saveToChatHistory(String userId, String role, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("role", role);
        params.put("message", message);
        
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertChatHistory", params);
        }
    }
    
    public void saveToChatHistory(String userId, String role, String message, String imageBase64) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("role", role);
        params.put("message", message);
        params.put("imageBase64", imageBase64);

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertChatHistory", params);
        }
    }

    // 🚨 4. getChatHistory 메서드: Object userId -> String userId로 변경
    public java.util.List<GetHistoryServlet.HistoryItem> getChatHistory(String userId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            return session.selectList(MAPPER_NS + ".selectChatHistory", params);
        }
    }
    
    public boolean insertAnalysis(AnalysisRecord record) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
        	System.out.println("[DEBUG] insertAnalysis 호출됨");
            System.out.println("[DEBUG] userId: " + record.getUserId());
            System.out.println("[DEBUG] text: " + record.getText());
            System.out.println("[DEBUG] inputType: " + record.getInputType());
        	
        	
        	int result = session.insert("com.tokkitalk.db.AnalysisMapper.insertAnalysis", record);
            System.out.println("[DEBUG] insert result count: " + result);
            
            return result > 0;
        } catch (Exception e) {
            System.err.println("[ERROR] insertAnalysis 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void deleteChatHistoryByIds(String userId, List<Integer> chatIds) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) { // true로 설정하여 자동 커밋
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("chatIds", chatIds);
            session.delete(MAPPER_NS + ".deleteChatHistoryByIds", params);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteChatHistoryByIds 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}