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
            if (row == null) return null;
            // 최소한의 복원: 저장된 전체 JSON을 우선 사용
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
        // 간단히 새 전체 JSON으로 덮어쓰기: userId는 보존 불가라 0L로 저장되니, 실제 서비스에 맞게 UPDATE로 교체 권장
        saveResult(updated, 0L);
    }

    public void insertFeedback(String analysisId, int liked, String partnerReaction) {
        Map<String, Object> params = new HashMap<>();
        params.put("analysis_id", analysisId);
        params.put("liked", liked);
        params.put("partner_reaction", partnerReaction);
        params.put("user_comment", null);

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertFeedback", params);
        }
    }

    public int deleteById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.delete(MAPPER_NS + ".deleteAnalysis", analysisId);
        }
    }
}


