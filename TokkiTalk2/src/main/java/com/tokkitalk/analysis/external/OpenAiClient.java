package com.tokkitalk.analysis.external;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tokkitalk.analysis.dto.AnalyzeRequest;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.dto.ResponseSuggestion;
import java.util.ArrayList;
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

        final String SYSTEM = "너는 한국어 대화 뉘앙스 분석가다.\n"
                + "규칙:\n"
                + "- 위 JSON 스키마로만 답한다(기타 텍스트 금지).\n"
                + "- 숨은 의도에는 근거 키워드 1~2개 포함(예: '역설표현','응답지연').\n"
                + "- 확신 낮으면 confidence<60, risk_flags에 이유 추가.\n"
                + "- 조언은 최소 3가지 스타일(무난·호감형, 구체 포인트형, 선택권 존중형). 문장은 짧고 바로 쓸 수 있게.\n";

        final String FEWSHOTS =
                "예시1_입력: \"나 신경 쓰지 말고 재밌게 놀아~\"\n" +
                "예시1_출력_JSON:{\n" +
                " \"surface\":\"괜찮다고 하지만 연락 기대\",\n" +
                " \"hidden\":\"확인요구. 근거: 역설표현\",\n" +
                " \"emotion\":{\"label\":\"서운함\",\"intensity\":3},\n" +
                " \"advice\":[\n" +
                "  {\"style\":\"무난·호감형\",\"text\":\"재밌게 놀되 짧게 안부만 남겨줘 😊\"},\n" +
                "  {\"style\":\"구체 포인트형\",\"text\":\"12시 전 한 번 연락 주면 더 좋아!\"},\n" +
                "  {\"style\":\"선택권 존중형\",\"text\":\"편한 대로 해~ 난 짧은 안부만 있으면 충분해\"}\n" +
                " ],\n" +
                " \"confidence\":82,\"risk_flags\":[]\n" +
                "}\n\n" +
                "예시2_입력: \"그냥 들어줘. 답 안 해도 돼.\"\n" +
                "예시2_출력_JSON:{\n" +
                " \"surface\":\"공감만 원함\",\n" +
                " \"hidden\":\"공감요구. 근거: '그냥 들어줘'\",\n" +
                " \"emotion\":{\"label\":\"답답함\",\"intensity\":4},\n" +
                " \"advice\":[\n" +
                "  {\"style\":\"무난·호감형\",\"text\":\"그랬구나, 많이 힘들었겠다.\"},\n" +
                "  {\"style\":\"구체 포인트형\",\"text\":\"지금은 조언 말고 네 기분부터 듣고 싶어.\"},\n" +
                "  {\"style\":\"선택권 존중형\",\"text\":\"괜찮아, 편한 만큼만 얘기해줘 🙂\"}\n" +
                " ],\n" +
                " \"confidence\":88,\"risk_flags\":[\"조언금지\"]\n" +
                "}\n";

        String userPrompt = "입력: \"" + (request != null ? String.valueOf(request.text) : "") + "\"\n아래 JSON 스키마로만 출력하라.";

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", SYSTEM);
        JsonObject shot = new JsonObject();
        shot.addProperty("role", "user");
        shot.addProperty("content", FEWSHOTS);
        JsonObject usr = new JsonObject();
        usr.addProperty("role", "user");
        usr.addProperty("content", userPrompt);

        JsonObject body = new JsonObject();
        body.addProperty("model", System.getProperty("openai.model", "gpt-4o"));
        body.addProperty("temperature", 0.6);
        body.addProperty("max_tokens", 700);
        body.add("messages", GSON.toJsonTree(new JsonObject[] { sys, shot, usr }));
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

            // Map new flat schema -> existing AnalysisResult structure
            JsonObject flat = GSON.fromJson(content, JsonObject.class);
            AnalysisResult out = new AnalysisResult();
            out.analysis_id = analysisId;

            // surface
            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            sm.one_line = flat.has("surface") && !flat.get("surface").isJsonNull()
                    ? flat.get("surface").getAsString() : "";
            double conf100 = flat.has("confidence") && !flat.get("confidence").isJsonNull()
                    ? flat.get("confidence").getAsDouble() : 75.0;
            sm.confidence = conf100 / 100.0;
            List<String> ev = new ArrayList<>();
            ev.add("LLM 분석 근거");
            sm.evidence = ev;
            out.surface_meaning = sm;

            // hidden
            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            hm.one_line = flat.has("hidden") && !flat.get("hidden").isJsonNull()
                    ? flat.get("hidden").getAsString() : "";
            hm.intent_labels = new ArrayList<>();
            hm.risk_flags = new ArrayList<>();
            if (flat.has("risk_flags") && flat.get("risk_flags").isJsonArray()) {
                for (JsonElement e : flat.getAsJsonArray("risk_flags")) {
                    if (e != null && !e.isJsonNull()) {
                        AnalysisResult.Flag f = new AnalysisResult.Flag();
                        f.flag = e.getAsString();
                        f.reason = "";
                        hm.risk_flags.add(f);
                    }
                }
            }
            hm.confidence = sm.confidence;
            out.hidden_meaning = hm;

            // emotion
            com.tokkitalk.analysis.dto.Emotion emo = new com.tokkitalk.analysis.dto.Emotion();
            if (flat.has("emotion") && flat.get("emotion").isJsonObject()) {
                JsonObject em = flat.getAsJsonObject("emotion");
                emo.label = em.has("label") && !em.get("label").isJsonNull() ? em.get("label").getAsString() : "중립";
                int intensity = em.has("intensity") && !em.get("intensity").isJsonNull() ? em.get("intensity").getAsInt() : 3;
                emo.valence = intensity - 3;
                emo.arousal = intensity / 5.0;
            } else {
                emo.label = "중립";
                emo.valence = 0; emo.arousal = 0.0;
            }
            emo.politeness_level = "";
            emo.cues = new java.util.HashMap<>();
            emo.confidence = sm.confidence;
            out.emotion = emo;

            // suggestion
            ResponseSuggestion rs = new ResponseSuggestion();
            rs.tone = "무난·호감형";
            rs.alternatives = new ArrayList<>();
            if (flat.has("advice") && flat.get("advice").isJsonArray()) {
                int i = 0;
                for (JsonElement e : flat.getAsJsonArray("advice")) {
                    if (e == null || e.isJsonNull()) continue;
                    JsonObject a = e.getAsJsonObject();
                    String style = a.has("style") && !a.get("style").isJsonNull() ? a.get("style").getAsString() : "";
                    String text = a.has("text") && !a.get("text").isJsonNull() ? a.get("text").getAsString() : "";
                    if (i == 0) { rs.primary = text; rs.tone = style; }
                    else { rs.alternatives.add(text); }
                    i++;
                }
            } else {
                rs.primary = "상대가 기분 좋게 느낄 짧은 답변을 제시";
            }
            rs.rationale = "LLM 생성";
            rs.confidence = sm.confidence;
            out.response_suggestion = rs;

            out.overall_confidence = sm.confidence;
            return out;
        }
    }
}


