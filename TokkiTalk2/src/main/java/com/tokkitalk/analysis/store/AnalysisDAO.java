package com.tokkitalk.analysis.store;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.FeedbackRequest;
import com.tokkitalk.analysis.GetHistoryServlet;

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

    public void saveResult(AnalysisResult result, Long userId) {
        AnalysisRecord rec = new AnalysisRecord();
        rec.analysisId = result.analysis_id;
        rec.inputType = "text"; // 현재는 텍스트만
        rec.text = result.surface_meaning != null ? result.surface_meaning.one_line : null;
        rec.imageUrl = null;
        rec.tone = (result.response_suggestion != null && result.response_suggestion.tone != null)
                ? result.response_suggestion.tone : "";
        rec.analysisResult = gson.toJson(result);
        rec.suggestions = result.response_suggestion != null ? gson.toJson(result.response_suggestion) : null;
        rec.userId = (userId != null ? userId : 0L);

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertAnalysis", rec);
        }
    }

    public AnalysisResult findById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AnalysisRecord row = session.selectOne(MAPPER_NS + ".selectAnalysis", analysisId);
            if (row == null)
                return null;
            if (row.analysisResult != null) {
                return gson.fromJson(row.analysisResult, AnalysisResult.class);
            }
            AnalysisResult result = new AnalysisResult();
            result.analysis_id = analysisId;
            return result;
        }
    }

    public java.util.List<AnalysisRecord> selectByUser(long userId, int offset, int limit) {
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
        saveResult(updated, 0L);
    }

    // 1. DeleteAnalysisServlet에서 호출할 메서드 추가 (기존 deleteById를 활용)
    public void delete(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete(MAPPER_NS + ".deleteAnalysis", analysisId);
        }
    }

    // 2. FeedbackServlet에서 호출할 메서드 추가
    public void saveFeedback(FeedbackRequest feedbackRequest) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> params = new HashMap<>();
            params.put("analysis_id", feedbackRequest.analysis_id);
            
            // Boolean 'like' 값을 정수(int)로 변환하여 'feedback_rating'에 저장
            int rating = feedbackRequest.like != null && feedbackRequest.like ? 1 : 0;
            params.put("feedback_rating", rating);
            
            params.put("feedback_comment", feedbackRequest.partner_reaction);
            session.update(MAPPER_NS + ".updateFeedback", params);
        }
    }

    public void saveToChatHistory(String userId, String role, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("role", role);
        params.put("message", message);
        
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertChatHistory", params);
        }
    }

    public java.util.List<GetHistoryServlet.HistoryItem> getChatHistory(String userId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            return session.selectList(MAPPER_NS + ".selectChatHistory", params);
        }
    }
}