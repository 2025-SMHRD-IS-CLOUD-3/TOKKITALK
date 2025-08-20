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
        this.model = System.getProperty("openai.model", "gpt-4o-mini");
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

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", "You are an analysis engine. Output ONLY a JSON object with EXACTLY these keys: surface_meaning (string), hidden_meaning (string), emotion (string), risk_flags (array of strings), suggested_reply (string), alternatives (array of 2 strings), confidence (number 0..1). Use Korean for all strings. Do not include any extra keys. The output must be valid JSON.");

        JsonObject usr = new JsonObject();
        usr.addProperty("role", "user");
        String prompt = "Analyze this message and return the JSON. Message: " + (request != null ? String.valueOf(request.text) : "");
        usr.addProperty("content", prompt);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", GSON.toJsonTree(new JsonObject[] { sys, usr }));
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

            // Map flat schema -> existing AnalysisResult structure
            JsonObject flat = GSON.fromJson(content, JsonObject.class);
            AnalysisResult out = new AnalysisResult();
            out.analysis_id = analysisId;

            // surface
            AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
            sm.one_line = flat.has("surface_meaning") && !flat.get("surface_meaning").isJsonNull()
                    ? flat.get("surface_meaning").getAsString() : "";
            sm.confidence = flat.has("confidence") && !flat.get("confidence").isJsonNull()
                    ? flat.get("confidence").getAsDouble() : 0.8;
            List<String> ev = new ArrayList<>();
            ev.add("LLM 분석 근거");
            sm.evidence = ev;
            out.surface_meaning = sm;

            // hidden
            AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
            hm.one_line = flat.has("hidden_meaning") && !flat.get("hidden_meaning").isJsonNull()
                    ? flat.get("hidden_meaning").getAsString() : "";
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
            emo.label = flat.has("emotion") && !flat.get("emotion").isJsonNull()
                    ? flat.get("emotion").getAsString() : "중립";
            emo.valence = 0;
            emo.arousal = 0.0;
            emo.politeness_level = "";
            emo.cues = new java.util.HashMap<>();
            emo.confidence = sm.confidence;
            out.emotion = emo;

            // suggestion
            ResponseSuggestion rs = new ResponseSuggestion();
            rs.tone = "기본";
            rs.primary = flat.has("suggested_reply") && !flat.get("suggested_reply").isJsonNull()
                    ? flat.get("suggested_reply").getAsString() : "";
            rs.alternatives = new ArrayList<>();
            if (flat.has("alternatives") && flat.get("alternatives").isJsonArray()) {
                for (JsonElement e : flat.getAsJsonArray("alternatives")) {
                    if (e != null && !e.isJsonNull()) {
                        rs.alternatives.add(e.getAsString());
                    }
                }
            }
            rs.rationale = "LLM 생성";
            rs.confidence = sm.confidence;
            out.response_suggestion = rs;

            out.overall_confidence = sm.confidence;
            return out;
        }
    }
}


