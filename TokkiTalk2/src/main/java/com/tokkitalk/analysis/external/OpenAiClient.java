package com.tokkitalk.analysis.external;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tokkitalk.analysis.dto.AnalyzeRequest;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.ResponseSuggestion;
import com.tokkitalk.analysis.dto.AnalysisOut;
import com.tokkitalk.analysis.util.AnalysisPostProcessor;
import com.tokkitalk.analysis.util.CsvFewshotsLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Minimal OpenAI client.
 * Reads API key from env var OPENAI_API_KEY or system property openai.api.key.
 */
public class OpenAiClient {
    private static final Gson GSON = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final String apiKey;
    private final String model;

    public OpenAiClient() {
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .build();
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getProperty("openai.api.key");
        }
        this.apiKey = key;
        this.model = System.getProperty("openai.model", "gpt-4o");
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Calls Chat Completions API and expects JSON content matching AnalysisResult schema.
     */
    public AnalysisResult analyzeWithLLM(String analysisId, AnalyzeRequest request) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("OPENAI_API_KEY not set");
        }

        final String SYSTEM = "너는 '여자어 번역기'다. 사용자가 보낸 문장을 여성 화자의 관점에서 해석해\n"
                + "남성이 이해하기 쉬운 '직설 번역'과 '상황별 대응'을 제시한다.\n\n"
                + "분석 방법:\n"
                + "1. 입력된 문장과 유사한 패턴을 학습 데이터에서 찾아라\n"
                + "2. 여성의 언어적 특성(간접표현, 맥락의존성, 감정표현)을 고려해라\n"
                + "3. 문화적·상황적 맥락을 반영해라\n"
                + "4. 만약 직접적인 예시가 없다면, 유사한 감정상태나 의도를 가진 예시를 참고해라\n\n"
                + "톤/스타일 지정:\n"
                + "친근하고 유머러스한 톤으로 답변하고, 여자의 감성을 고려한다. "
                + "여자는 직접적 조언보다 위로와 공감을 좋아한다. "
                + "성별 고정관념을 조장하지 않도록 주의한다.\n"
                + "실제 상황에서 도움이 되는 실용적인 조언을 제공한다.\n\n"
                + "구체적인 출력 형식:\n"
                + "🔍 **표면적 의미**: (문장 그대로의 뜻)\n"
                + "💡 **숨은 의도**: (실제로 전달하고자 하는 메시지)\n"
                + "❤️ **감정 상태**: (현재 감정 분석)\n"
                + "💬 **추천 대응**: (상황별 적절한 반응)\n\n"
                + "학습 데이터 활용 지침:\n"
                + "- 제공된 예시들을 패턴 분석의 기준으로 사용해라\n"
                + "- 입력과 정확히 일치하는 예시가 없어도, 감정이나 의도가 유사한 케이스를 참고해라\n"
                + "- 예시에서 보여준 대응 스타일을 일관성 있게 적용해라\n"
                + "- 새로운 입력이라도 학습된 패턴을 조합해서 적절한 분석을 제공해라\n\n"
                + "제약 조건:\n"
                + "- 개인차가 있음을 인정하고 일반화하지 않는다\n"
                + "- 건전하고 건설적인 관계 조언에 집중한다\n"
                + "- 성별 갈등을 조장하지 않는다\n"
                + "- 확신이 낮은 경우 confidence를 낮추고 risk_flags에 이유를 명시한다\n\n"
                + "[출력 형식 — JSON만]\n"
                + "{\n"
                + "  \"surface\": \"<표면적 의미 한 줄(문자 그대로)>\",\n"
                + "  \"hidden\": \"<여자어에 숨은 진짜 의도·동기(근거 키워드 1~2개 포함)>\",\n"
                + "  \"emotion\": {\"label\":\"호감|서운함|혼란|기쁨|분노|불안|체념|중립\",\"intensity\":1-5},\n"
                + "  \"translation\": \"<여자어를 남자어/직설 표현으로 짧게 번역>\",\n"
                + "  \"advice\": [\n"
                + "    {\"style\":\"관심표현형\",\"text\":\"...\"},\n"
                + "    {\"style\":\"위로·공감형\",\"text\":\"...\"},\n"
                + "    {\"style\":\"재치있는 응답형\",\"text\":\"...\"},\n"
                + "    {\"style\":\"구체적행동형\",\"text\":\"...\"}\n"
                + "  ],\n"
                + "  \"confidence\": 0-100,\n"
                + "  \"risk_flags\": [],\n"
                + "  \"similar_pattern\": \"<학습 데이터에서 참고한 유사 패턴이나 케이스>\"\n"
                + "}\n\n"
                + "[엄격한 규칙]\n"
                + "- 반드시 위 JSON만 출력(앞뒤 설명·마크다운 금지).\n"
                + "- 'advice'는 반드시 3~4개로 생성. 서로 다른 스타일/내용, 중복 금지.\n"
                + "- 각 advice.text는 '20~60자'의 구체적인 대화 문장과 행동 가이드 포함. 실용적이고 상세하게.\n"
                + "- 'hidden'에는 근거 키워드 1~2개(예: 역설표현, 응답지연, 선택요구, 질투암시)를 괄호로 포함.\n"
                + "- 확신이 낮으면 confidence<60으로 내리고 risk_flags에 이유 추가(예: [\"맥락부족\", \"패턴불일치\"]).\n"
                + "- 학습 데이터에 없는 새로운 패턴이면 similar_pattern에 '새로운 패턴' 명시.\n"
                + "- 성별 고정관념·단정적 일반화 지양. 개인차 전제를 명시.";

        // CSV 파일에서 fewshots 데이터 로드
        final String FEWSHOTS = CsvFewshotsLoader.loadFewshotsFromCsv();

        String userPrompt = "입력: \"" + (request != null ? String.valueOf(request.text) : "") + "\"\n\n"
                + "위의 학습 예시들을 참고하여 이 문장을 분석해라. "
                + "정확히 일치하는 예시가 없다면 유사한 감정이나 의도를 가진 케이스를 참고하여 "
                + "일관된 분석 패턴을 적용해라. 아래 JSON 스키마로만 출력하라.";

        // Prompt mode toggle: default advanced schema, optional 'korean5' few-shot simple schema
        final String promptMode = System.getProperty("tokki.promptMode", "advanced");
        JsonObject sys = new JsonObject();
        JsonObject shot = new JsonObject();
        JsonObject usr = new JsonObject();
        if ("korean5".equalsIgnoreCase(promptMode)) {
            // Build one user message with few-shot examples and schema (Korean keys)
            StringBuilder sb = new StringBuilder();
            sb.append("당신은 인간 커뮤니케이션, 특히 남녀 간의 언어적 차이에 대한 심리학적, 생물학적, 사회문화적 요인을 깊이 이해하고 있는 AI 전문가입니다. \"여자언어 번역기\" 역할을 수행해주세요.\n\n");
            sb.append("임무: 여성이 주로 사용하는 문구나 진술을 받으면, 그 문구의 내재된 의미와 의도를 남성이 이해하기 쉬운 방식으로 해석하여 제공합니다.\n\n");
            sb.append("출력 포맷:\n{\n  \"표면적 의미\": \"남성이 오해할 수 있는 방식\",\n  \"숨은 의도\": \"여성의 실제 심리적 의미\",\n  \"감정 상태\": \"여성이 느끼는 감정\",\n  \"TOKKI의 제안\": \"남성의 반응 방법\"\n}\n\n");
            // 5 examples
            sb.append("예시 1:\n사용자: \"앞머리 좀 길게 자르면 어떻게 되지?\"\n응답:\n{\n  \"표면적 의미\": \"그냥 길면 뒤로 넘기면 되지. (단순히 머리카락 길이에 대한 질문으로 이해)\",\n  \"숨은 의도\": \"나는 스타일이나 이미지 변화를 고민하고 있어. 내 변화에 관심을 가져주고 자세한 의견을 줘.\",\n  \"감정 상태\": \"고민\",\n  \"TOKKI의 제안\": \"어떤 스타일을 생각하고 있어? 네가 고민하는 모습이 귀여워 😊\"\n}\n\n");
            sb.append("예시 2:\n사용자: \"나 오늘 피곤해.\"\n응답:\n{\n  \"표면적 의미\": \"그냥 피곤하다는 상태를 알리는 말이구나. (단순한 피로 상태 고지)\",\n  \"숨은 의도\": \"나는 지금 위로나 배려, 공감을 받고 싶어. 내 피로를 이해해 주기를 바라.\",\n  \"감정 상태\": \"피곤함\",\n  \"TOKKI의 제안\": \"많이 힘들었구나. 내가 옆에서 쉬게 해줄게 😊\"\n}\n\n");
            sb.append("예시 3:\n사용자: \"그냥 아무거나.\" (식당에서)\n응답:\n{\n  \"표면적 의미\": \"정말 아무거나 괜찮다는 뜻이구나. 내가 알아서 고르면 되겠네.\",\n  \"숨은 의도\": \"나는 사실 원하는 것이 있지만, 당신이 나를 위해 고민하고 배려해서 선택해주기를 바라.\",\n  \"감정 상태\": \"기대\",\n  \"TOKKI의 제안\": \"그럼 내가 맛있는 걸 골라줄게! 😊\"\n}\n\n");
            sb.append("예시 4:\n사용자: \"나 신경 쓰지 말고 재밌게 놀아~\"\n응답:\n{\n  \"표면적 의미\": \"괜찮다고 하지만 연락 기대\",\n  \"숨은 의도\": \"사실은 연락을 기대하고 있음 (역설적 표현)\",\n  \"감정 상태\": \"서운함\",\n  \"TOKKI의 제안\": \"재밌게 놀되 짧게 안부만 남겨줘 😊\"\n}\n\n");
            sb.append("예시 5:\n사용자: \"밥 먹었어?\"\n응답:\n{\n  \"표면적 의미\": \"밥을 먹었는지 묻는다.\",\n  \"숨은 의도\": \"상대방의 건강과 일상을 챙기려는 마음\",\n  \"감정 상태\": \"배려와 애정\",\n  \"TOKKI의 제안\": \"간단히 먹은 걸 공유하고 따뜻하게 답하세요.\"\n}\n\n");
            sb.append("이제 아래 문장을 같은 방식으로 해석해라.\n\n사용자: \"")
              .append(request != null ? String.valueOf(request.text) : "")
              .append("\"\n");

            usr.addProperty("role", "user");
            usr.addProperty("content", sb.toString());
        } else {
            sys.addProperty("role", "system");
            sys.addProperty("content", SYSTEM);
            shot.addProperty("role", "user");
            shot.addProperty("content", FEWSHOTS);
            usr.addProperty("role", "user");
            usr.addProperty("content", userPrompt);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", System.getProperty("openai.model", "gpt-4o"));
        body.addProperty("temperature", 0.6);
        body.addProperty("max_tokens", 700);
        if ("korean5".equalsIgnoreCase(promptMode)) {
            body.add("messages", GSON.toJsonTree(new JsonObject[] { usr }));
        } else {
            body.add("messages", GSON.toJsonTree(new JsonObject[] { sys, shot, usr }));
        }
        JsonObject respFormat = new JsonObject();
        respFormat.addProperty("type", "json_object");
        body.add("response_format", respFormat);

        Request httpReq = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(GSON.toJson(body), JSON))
                .build();

        try (Response httpResp = http.newCall(httpReq).execute()) {
            if (!httpResp.isSuccessful()) {
                throw new IOException("OpenAI HTTP " + httpResp.code() + ": " + (httpResp.body() != null ? httpResp.body().string() : ""));
            }
            String resp = httpResp.body() != null ? httpResp.body().string() : "";
            JsonObject root = GSON.fromJson(resp, JsonObject.class);
            JsonElement contentEl = root
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content");
            String content = contentEl != null ? contentEl.getAsString() : "{}";

            // Parse flat JSON response into AnalysisOut DTO
            AnalysisOut out = GSON.fromJson(content, AnalysisOut.class);
            if (out == null) {
                throw new IOException("Failed to parse OpenAI response");
            }

            // Post-process and validate
            AnalysisPostProcessor.validateAndFix(out);

            // Convert AnalysisOut to AnalysisResult DTO
            AnalysisResult result = new AnalysisResult();
            result.analysis_id = analysisId;

            // Surface meaning
            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            sm.one_line = out.surface != null ? out.surface : "";
            sm.confidence = out.confidence != null ? out.confidence / 100.0 : 0.85;
            sm.evidence = Arrays.asList("GPT 분석");
            result.surface_meaning = sm;

            // Hidden meaning
            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            hm.one_line = out.hidden != null ? out.hidden : "";
            // translation 필드를 hidden meaning에 포함
            if (out.translation != null && !out.translation.isEmpty()) {
                hm.one_line += " (" + out.translation + ")";
            }
            // similar_pattern 정보를 hidden meaning에 포함
            if (out.similar_pattern != null && !out.similar_pattern.isEmpty()) {
                hm.one_line += " [참고패턴: " + out.similar_pattern + "]";
            }
            hm.intent_labels = Arrays.asList(new AnalysisResult.LabelScore("GPT 분석", 0.8));
            hm.risk_flags = new ArrayList<>();
            if (out.risk_flags != null) {
                for (String flag : out.risk_flags) {
                    AnalysisResult.Flag f = new AnalysisResult.Flag();
                    f.flag = flag;
                    f.reason = "";
                    hm.risk_flags.add(f);
                }
            }
            hm.confidence = sm.confidence;
            result.hidden_meaning = hm;

            // Emotion
            com.tokkitalk.analysis.dto.Emotion emo = new com.tokkitalk.analysis.dto.Emotion();
            if (out.emotion != null) {
                emo.label = out.emotion.label != null ? out.emotion.label : "중립";
                int intensity = out.emotion.intensity != null ? out.emotion.intensity : 3;
                emo.valence = intensity - 3;
                emo.arousal = intensity / 5.0;
            } else {
                emo.label = "중립";
                emo.valence = 0; emo.arousal = 0.0;
            }
            emo.politeness_level = "해요체";
            emo.cues = new java.util.HashMap<>();
            emo.confidence = sm.confidence;
            result.emotion = emo;

            // Response suggestion - Convert advice to ResponseSuggestion
            ResponseSuggestion rs = new ResponseSuggestion();
            rs.tone = "상큼";
            rs.primary = out.advice != null && !out.advice.isEmpty() ? out.advice.get(0).text : "GPT 분석 결과";
            rs.alternatives = new ArrayList<>();
            if (out.advice != null && out.advice.size() > 1) {
                for (int i = 1; i < out.advice.size(); i++) {
                    rs.alternatives.add(out.advice.get(i).text);
                }
            }
            rs.rationale = "GPT 분석";
            rs.confidence = sm.confidence;
            result.response_suggestion = rs;
            
            // Advice 배열을 직접 포함 (스타일 정보 유지)
            result.advice = new ArrayList<>();
            if (out.advice != null) {
                for (AnalysisOut.Suggestion suggestion : out.advice) {
                    AnalysisResult.AdviceItem item = new AnalysisResult.AdviceItem();
                    item.style = suggestion.style != null ? suggestion.style : "제안";
                    item.text = suggestion.text != null ? suggestion.text : "";
                    result.advice.add(item);
                }
            }

            result.overall_confidence = sm.confidence;
            return result;
        }
    }
}


