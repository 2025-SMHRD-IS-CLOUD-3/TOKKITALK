package com.tokkitalk.analysis.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokkitalk.analysis.dto.AnalysisResult;

public class AnalysisDAO {
    private final SqlSessionFactory sqlSessionFactory;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public AnalysisDAO() {
        try {
            String resource = "mybatis-db.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveResult(AnalysisResult result) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> params = new HashMap<>();
            params.put("analysis_id", result.analysis_id);
            params.put("literal_summary", result.surface_meaning != null ? result.surface_meaning.one_line : null);
            params.put("implied_intent", result.hidden_meaning != null ? result.hidden_meaning.one_line : null);
            params.put("risk_flags", result.hidden_meaning != null ? gson.toJson(result.hidden_meaning.risk_flags) : null);
            params.put("emotion_label", result.emotion != null ? result.emotion.label : null);
            params.put("emotion_valence", result.emotion != null ? result.emotion.valence : null);
            params.put("emotion_arousal", result.emotion != null ? result.emotion.arousal : null);
            params.put("politeness_level", result.emotion != null ? result.emotion.politeness_level : null);
            params.put("cues_json", result.emotion != null ? gson.toJson(result.emotion.cues) : null);
            params.put("suggestion_main", result.response_suggestion != null ? result.response_suggestion.primary : null);
            params.put("suggestion_alt", result.response_suggestion != null ? gson.toJson(result.response_suggestion.alternatives) : null);
            params.put("suggestion_tone", result.response_suggestion != null ? result.response_suggestion.tone : null);
            params.put("rationale", result.response_suggestion != null ? result.response_suggestion.rationale : null);
            params.put("overall_confidence", result.overall_confidence);
            params.put("model_version", "mvp-0.1");
            session.insert("com.tokkitalk.db.AnalysisMapper.insertAnalysis", params);
        }
    }

    public void updateSuggestion(AnalysisResult result) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> params = new HashMap<>();
            params.put("analysis_id", result.analysis_id);
            params.put("suggestion_main", result.response_suggestion != null ? result.response_suggestion.primary : null);
            params.put("suggestion_alt", result.response_suggestion != null ? gson.toJson(result.response_suggestion.alternatives) : null);
            params.put("suggestion_tone", result.response_suggestion != null ? result.response_suggestion.tone : null);
            session.update("com.tokkitalk.db.AnalysisMapper.updateSuggestion", params);
        }
    }

    public void insertFeedback(String analysisId, int liked, String reaction) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> params = new HashMap<>();
            params.put("analysis_id", analysisId);
            params.put("liked", liked);
            params.put("partner_reaction", reaction);
            session.insert("com.tokkitalk.db.AnalysisMapper.insertFeedback", params);
        }
    }

    public AnalysisResult findById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> row = session.selectOne("com.tokkitalk.db.AnalysisMapper.selectAnalysis", analysisId);
            if (row == null) return null;
            AnalysisResult r = new AnalysisResult();
            r.analysis_id = analysisId;
            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            Object literal = row.get("literal_summary");
            if (literal == null) literal = row.get("LITERAL_SUMMARY");
            sm.one_line = literal != null ? literal.toString() : null;
            r.surface_meaning = sm;
            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            Object implied = row.get("implied_intent");
            if (implied == null) implied = row.get("IMPLIED_INTENT");
            hm.one_line = implied != null ? implied.toString() : null;
            r.hidden_meaning = hm;
            return r;
        }
    }

    public int deleteById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.delete("com.tokkitalk.db.AnalysisMapper.deleteAnalysis", analysisId);
        }
    }
}


