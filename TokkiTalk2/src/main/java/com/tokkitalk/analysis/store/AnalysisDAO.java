package com.tokkitalk.analysis.store;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.ResponseSuggestion;

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

    public void saveResult(AnalysisResult result) {
        Map<String, Object> params = new HashMap<>();
        params.put("analysis_id", result.analysis_id);

        // surface meaning
        String literalSummary = result.surface_meaning != null ? result.surface_meaning.one_line : null;
        params.put("literal_summary", literalSummary);

        // hidden meaning
        String impliedIntent = result.hidden_meaning != null ? result.hidden_meaning.one_line : null;
        params.put("implied_intent", impliedIntent);

        // risk flags (store count or simple string)
        String riskFlags = result.hidden_meaning != null && result.hidden_meaning.risk_flags != null
                ? String.valueOf(result.hidden_meaning.risk_flags.size())
                : null;
        params.put("risk_flags", riskFlags);

        // emotion
        params.put("emotion_label", result.emotion != null ? result.emotion.label : null);
        params.put("emotion_valence", result.emotion != null ? Integer.valueOf(result.emotion.valence) : null);
        params.put("emotion_arousal", result.emotion != null ? Double.valueOf(result.emotion.arousal) : null);
        params.put("politeness_level", result.emotion != null ? result.emotion.politeness_level : null);
        params.put("cues_json", result.emotion != null && result.emotion.cues != null ? gson.toJson(result.emotion.cues) : null);

        // suggestion
        String suggestionMain = result.response_suggestion != null ? result.response_suggestion.primary : null;
        params.put("suggestion_main", suggestionMain);
        List<String> alts = result.response_suggestion != null ? result.response_suggestion.alternatives : null;
        params.put("suggestion_alt", (alts != null && !alts.isEmpty()) ? String.join("|", alts) : null);
        params.put("suggestion_tone", result.response_suggestion != null ? result.response_suggestion.tone : null);
        params.put("rationale", result.response_suggestion != null ? result.response_suggestion.rationale : null);

        // overall
        params.put("overall_confidence", result.overall_confidence);
        params.put("model_version", "mvp-heuristic-1");

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(MAPPER_NS + ".insertAnalysis", params);
        }
    }

    public AnalysisResult findById(String analysisId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> row = session.selectOne(MAPPER_NS + ".selectAnalysis", analysisId);
            if (row == null) {
                return null;
            }

            AnalysisResult result = new AnalysisResult();
            result.analysis_id = analysisId;

            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            sm.one_line = (String) row.get("literal_summary");
            result.surface_meaning = sm;

            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            hm.one_line = (String) row.get("implied_intent");
            result.hidden_meaning = hm;

            // Suggestion not loaded by this query; keep null to allow safe defaults upstream
            result.response_suggestion = null;

            return result;
        }
    }

    public void updateSuggestion(AnalysisResult updated) {
        if (updated == null || updated.response_suggestion == null) {
            return;
        }
        ResponseSuggestion s = updated.response_suggestion;
        Map<String, Object> params = new HashMap<>();
        params.put("analysis_id", updated.analysis_id);
        params.put("suggestion_main", s.primary);
        List<String> alts = s.alternatives;
        params.put("suggestion_alt", (alts != null && !alts.isEmpty()) ? String.join("|", alts) : null);
        params.put("suggestion_tone", s.tone);

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update(MAPPER_NS + ".updateSuggestion", params);
        }
    }

    public void insertFeedback(String analysisId, int liked, String partnerReaction) {
        Map<String, Object> params = new HashMap<>();
        params.put("analysis_id", analysisId);
        params.put("liked", liked);
        params.put("partner_reaction", partnerReaction);

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


