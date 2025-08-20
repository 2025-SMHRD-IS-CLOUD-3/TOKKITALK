package com.tokkitalk.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import com.tokkitalk.analysis.dto.AnalyzeRequest;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.Emotion;
import com.tokkitalk.analysis.dto.FeedbackRequest;
import com.tokkitalk.analysis.dto.ResponseSuggestion;
import com.tokkitalk.analysis.dto.SuggestRequest;
import com.tokkitalk.analysis.dto.SuggestResult;
import com.tokkitalk.analysis.store.AnalysisDAO;
import com.tokkitalk.analysis.external.OpenAiClient;

/**
 * High level orchestration service. MVP: uses simple heuristics and placeholders.
 * Later: plug actual OpenAI calls with structured outputs and escalation.
 */
public class AnalysisService {

    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final OpenAiClient openAiClient = new OpenAiClient();

    public AnalysisResult analyze(String analysisId, AnalyzeRequest request, Long userId) {
        AnalysisResult result = null;
        // Try LLM if configured; fallback to heuristic
        if (openAiClient.isConfigured()) {
            try {
                result = openAiClient.analyzeWithLLM(analysisId, request);
            } catch (Exception e) {
                // swallow and fallback
                result = null;
            }
        }
        if (result == null) {
            // 휴리스틱 폴백 - 새로운 구조 적용
            result = new AnalysisResult();
            result.analysis_id = analysisId;

            // Surface meaning
            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            sm.one_line = (request.text != null && !request.text.isEmpty()) ? request.text.substring(0, Math.min(30, request.text.length())) : "요약 없음";
            sm.confidence = 0.86;
            sm.evidence = Arrays.asList("텍스트 길이", "문장 패턴");
            result.surface_meaning = sm;

            // Hidden meaning
            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            hm.one_line = "은근한 호감 신호로 보임";
            hm.intent_labels = Arrays.asList(new AnalysisResult.LabelScore("호감테스트", 0.78));
            hm.risk_flags = Arrays.asList();
            hm.confidence = 0.72;
            result.hidden_meaning = hm;

            // Emotion
            Emotion em = new Emotion();
            em.label = "긍정";
            em.valence = 2;
            em.arousal = 0.4;
            em.politeness_level = "해요체";
            Map<String, Object> cues = new HashMap<>();
            cues.put("laughter_level", 0);
            cues.put("cry_level", 0);
            cues.put("emoji_count", 1);
            cues.put("reply_delay_minutes", 5);
            em.cues = cues;
            em.confidence = 0.81;
            result.emotion = em;

            // Suggestion - 확장된 데이터셋 기반 휴리스틱 제안 (더 구체적이고 긴 제안들)
            ResponseSuggestion rs = new ResponseSuggestion();
            rs.tone = request.options != null && request.options.tone != null ? request.options.tone : "친근";
            rs.primary = "완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?";
            rs.alternatives = Arrays.asList(
                "많이 힘들었구나, 고생했어 😞 내가 옆에서 쉬게 해줄게",
                "위험한 질문이네 😂 난 이거 한 표! 기분에 맞춰 정하자!",
                "내가 맛있는 거라도 사줄까? 🍕 오늘은 특별히 예쁘니까"
            );
            rs.rationale = "확장된 데이터셋 기반 분석";
            rs.confidence = 0.85;
            result.response_suggestion = rs;
            
            // Advice 배열도 추가 (스타일 정보 포함)
            result.advice = new ArrayList<>();
            AnalysisResult.AdviceItem item1 = new AnalysisResult.AdviceItem();
            item1.style = "관심표현형";
            item1.text = "완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?";
            result.advice.add(item1);
            
            AnalysisResult.AdviceItem item2 = new AnalysisResult.AdviceItem();
            item2.style = "위로·공감형";
            item2.text = "많이 힘들었구나, 고생했어 😞 내가 옆에서 쉬게 해줄게";
            result.advice.add(item2);
            
            AnalysisResult.AdviceItem item3 = new AnalysisResult.AdviceItem();
            item3.style = "재치있는 응답형";
            item3.text = "위험한 질문이네 😂 난 이거 한 표! 기분에 맞춰 정하자!";
            result.advice.add(item3);
            
            AnalysisResult.AdviceItem item4 = new AnalysisResult.AdviceItem();
            item4.style = "구체적행동형";
            item4.text = "내가 맛있는 거라도 사줄까? 🍕 오늘은 특별히 예쁘니까";
            result.advice.add(item4);

            result.overall_confidence = 0.80;
        }

        // Persist minimal record
        analysisDAO.saveResult(result, userId);
        return result;
    }

    public SuggestResult regenerateSuggestion(SuggestRequest req) {
        AnalysisResult existing = analysisDAO.findById(req.analysis_id);
        SuggestResult out = new SuggestResult();
        out.tone = req.tone != null ? req.tone : (existing != null && existing.response_suggestion != null ? existing.response_suggestion.tone : "기본");
        out.primary = "톤(" + out.tone + ")에 맞춘 새로운 제안 문구";
        out.alternatives = Arrays.asList("대안1", "대안2");

        // update suggestion fields on existing analysis
        if (existing != null) {
            ResponseSuggestion rs = new ResponseSuggestion();
            rs.tone = out.tone;
            rs.primary = out.primary;
            rs.alternatives = out.alternatives;
            rs.rationale = existing.response_suggestion != null ? existing.response_suggestion.rationale : "";
            rs.confidence = existing.response_suggestion != null ? existing.response_suggestion.confidence : 0.7;
            existing.response_suggestion = rs;
            analysisDAO.updateSuggestion(existing);
        }

        return out;
    }

    public void saveFeedback(FeedbackRequest req) {
        analysisDAO.insertFeedback(req.analysis_id, req.like != null && req.like ? 1 : 0, req.partner_reaction);
    }

    public boolean deleteAnalysis(String analysisId) {
        return analysisDAO.deleteById(analysisId) > 0;
    }
}


