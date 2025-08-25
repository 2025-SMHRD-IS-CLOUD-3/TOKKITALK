package com.tokkitalk.analysis.external;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Minimal OpenAI client. Reads API key from env var OPENAI_API_KEY or system
 * property openai.api.key.
 */
public class OpenAiClient {
    private static final Gson GSON = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final String apiKey;
    private final String model;
    
    // 재시도 로직을 위한 변수 선언
    private static final int MAX_RETRIES = 5; // 재시도 횟수 3 -> 5로 증가
    private static final long INITIAL_DELAY_SECONDS = 5;

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
        // YOUR_API_KEY_HERE 문자열 비교 제거
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Calls Chat Completions API and expects JSON content matching AnalysisResult
     * schema.
     */
    public AnalysisResult analyzeWithLLM(String text, String imageBase64) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("OPENAI_API_KEY not set");
        }

        final String SYSTEM_PROMPT = "너는 '여자어 번역기'다. 사용자가 보낸 문장이나 이미지에 있는 텍스트를 분석해 "
                + "남성이 이해하기 쉬운 '직설 번역'과 '상황별 대응'을 제시한다. "
                + "이미지가 오면 먼저 텍스트를 추출(OCR)하고, 대화의 화자/시간/흐름을 파악해라.\n\n"

                + "분석 원칙:\n"
                + "1) 발화 맥락을 필수 반영: a) 누가 선톡했는지, b) 직전 연락 공백, c) 시각(아침/밤), d) 대화 흐름(질문→응답 지연→감탄사 등). "
                + "2) 여성 화자의 역설/간접표현을 고려하되, 개인차 전제를 명시. "
                + "3) 남성이 놓치기 쉬운 신호(관심기대, 확인요구, 서운함 유발 원인)를 설명.\n\n"

                + "맥락 체크리스트:\n"
                + "□ 대화 시작자: 여자가 먼저 연락? 남자가 먼저 연락?\n"
                + "□ 시간대: 아침/점심/저녁/밤 (감정에 영향)\n"
                + "□ 직전 상황: 언제 마지막 연락? 공백 기간?\n"
                + "□ 대화 흐름: 질문→답변 지연→감탄사/이모티콘 순서\n"
                + "□ 문맥 단서: 특별한 날/약속/이전 대화 내용\n"
                + "□ 감정 표현: 이모티콘, 특수문자, 반복 표현\n\n"

                + "반언어 신호 해석 규칙:\n"
                + "- \"...\" 개수(1:여운, 2~3:머뭇/서운, 4+:감정 누름/거리두기), \"ㅋ\"(1:비꼼/방어, 2~3:친근농담, 4+:가벼운 장난), \"ㅎ\"(완곡/부드러움)\n"
                + "- 마침표/틸드/이모지 등도 맥락에 따라 감정의 세기를 조정\n\n"

                + "서술 규칙:\n"
                + "- hidden(숨은 의도)은 반드시 '상황 설명'과 '신호 해석' 1문장을 포함하여 2~3문장 서술: "
                +   "예) '아침부터 연락이 없었고 여자가 먼저 톡을 시작했다 → 남자는 이미 일어났고 밥까지 먹었는데도 먼저 연락이 없었다 → 그래서 서운함이 생겼다(관심기대) → …이 3개라 머뭇·서운함이 반영됨'. "
                + "- translation(직설 번역)은 1줄로 요지: '아침에 먼저 안부를 보내줘'. "
                + "- advice는 3~4개, 서로 다른 스타일이며 '말+행동 약속'이 함께 들어가야 함. "
                + "  · 관심표현형: 공감/사과 + 현재 감정 인정\n"
                + "  · 행동개선형: 구체적 재발방지 약속(예: 내일부터 아침 먼저 톡)\n"
                + "  · 위로·공감형: 감정명시+배려\n"
                + "  · 재치형(선택): 가볍게 긴장 완화하되 문제를 가리지 말 것\n"
                + "- emotion.cues에 반언어 신호 정보를 포함: {ellipsis: \"...개수\", k: \"ㅋ개수\", h: \"ㅎ개수\"} (없어도 무방)\n"
                + "- 확신이 낮으면 confidence<60, risk_flags에 ['맥락부족','OCR불확실'] 등 사유 기입.\n"
                + "- 성별 고정관념/단정 지양, 개인차 전제를 명시.\n\n"

                + "학습 데이터 활용 지침:\n"
                + "- 제공된 예시들을 패턴 분석의 기준으로 사용해라\n"
                + "- 입력과 정확히 일치하는 예시가 없어도, 감정이나 의도가 유사한 케이스를 참고해라\n"
                + "- 예시에서 보여준 대응 스타일을 일관성 있게 적용해라\n"
                + "- 새로운 입력이라도 학습된 패턴을 조합해서 적절한 분석을 제공해라\n\n"

                + "제약 조건:\n"
                + "- 개인차가 있음을 인정하고 일반화하지 않는다\n"
                + "- 건전하고 건설적인 관계 조언에 집중한다\n"
                + "- 성별 갈등을 조장하지 않는다\n"
                + "- 확신이 낮은 경우 confidence를 낮추고 risk_flags에 이유를 명시한다\n"
                + "- 학습 데이터에 없는 새로운 패턴이면 similar_pattern에 '새로운 패턴' 명시.\n"
                + "- 성별 고정관념·단정적 일반화 지양. 개인차 전제를 명시.\n\n"

                + "응답은 반드시 JSON으로만 출력.";

        final String FEWSHOTS = CsvFewshotsLoader.loadFewshotsFromCsv();

        if ((text == null || text.isEmpty()) && (imageBase64 == null || imageBase64.isEmpty())) {
            throw new IllegalArgumentException("No text or image provided for analysis.");
        }

        // 이미지 해상도를 낮추는 로직 추가
        String resizedImageBase64 = imageBase64;
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                // Base64 문자열을 이미지로 디코딩
                byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // 최대 너비와 높이 설정 (가로세로 비율을 유지하며 크기 조절)
                int newWidth = originalImage.getWidth();
                int newHeight = originalImage.getHeight();
                double maxDim = 1024.0; // OpenAI 권장 해상도에 맞춰 최대 크기 1024 설정

                if (newWidth > maxDim || newHeight > maxDim) {
                    double aspectRatio = (double) originalImage.getWidth() / (double) originalImage.getHeight();
                    if (newWidth > newHeight) {
                        newWidth = (int) maxDim;
                        newHeight = (int) (maxDim / aspectRatio);
                    } else {
                        newHeight = (int) maxDim;
                        newWidth = (int) (maxDim * aspectRatio);
                    }
                }

                // 이미지 리사이징
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                resizedImage.getGraphics().drawImage(originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

                // 리사이징된 이미지를 다시 Base64 문자열로 인코딩
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpeg", baos);
                byte[] resizedBytes = baos.toByteArray();
                resizedImageBase64 = Base64.getEncoder().encodeToString(resizedBytes);
            } catch (Exception e) {
                System.err.println("이미지 리사이징 중 오류 발생. 원본 이미지 사용: " + e.getMessage());
                // 오류 발생 시 원본 이미지를 그대로 사용
                resizedImageBase64 = imageBase64;
            }
        }
        
        int retryCount = 0;
        long delaySeconds = INITIAL_DELAY_SECONDS;

        while (true) {
            JsonArray messages = new JsonArray();
            JsonObject sysMsg = new JsonObject();
            sysMsg.addProperty("role", "system");
            sysMsg.addProperty("content", SYSTEM_PROMPT);
            messages.add(sysMsg);

            JsonObject shotMsg = new JsonObject();
            shotMsg.addProperty("role", "user");
            shotMsg.addProperty("content", FEWSHOTS);
            messages.add(shotMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            JsonArray contentArray = new JsonArray();
            
            String combinedPrompt = "분석할 텍스트: " + (text != null && !text.isEmpty() ? text : "없음") + "\n";
            if (resizedImageBase64 != null && !resizedImageBase64.isEmpty()) {
                combinedPrompt += "이미지에 포함된 텍스트를 읽고 함께 분석해줘.";
            }
            
            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            textContent.addProperty("text", combinedPrompt);
            contentArray.add(textContent);

            if (resizedImageBase64 != null && !resizedImageBase64.isEmpty()) {
                JsonObject imageContent = new JsonObject();
                imageContent.addProperty("type", "image_url");
                JsonObject imageUrlObject = new JsonObject();
                imageUrlObject.addProperty("url", "data:image/jpeg;base64," + resizedImageBase64);
                imageContent.add("image_url", imageUrlObject);
                contentArray.add(imageContent);
            }

            userMsg.add("content", contentArray);
            messages.add(userMsg);

            JsonObject body = new JsonObject();
            body.addProperty("model", this.model);
            body.addProperty("temperature", 0.6);
            body.addProperty("max_tokens", 700);
            body.add("messages", messages);

            JsonObject respFormat = new JsonObject();
            respFormat.addProperty("type", "json_object");
            body.add("response_format", respFormat);

            Request httpReq = new Request.Builder().url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(GSON.toJson(body), JSON)).build();

            try (Response httpResp = http.newCall(httpReq).execute()) {
                String responseBodyString = httpResp.body().string();
                
                if (httpResp.code() == 429) {
                    System.err.println("API 호출량 제한 초과 (429). " + delaySeconds + "초 후 재시도합니다.");
                    try {
                        Thread.sleep(delaySeconds * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("재시도 대기 중 인터럽트 발생", e);
                    }
                    delaySeconds *= 2; // 지수적 백오프
                    retryCount++;
                    if (retryCount >= MAX_RETRIES) {
                        throw new IOException("최대 재시도 횟수 (" + MAX_RETRIES + ") 초과.");
                    }
                    continue; // while 루프의 처음으로 돌아가서 재시도
                }

                if (!httpResp.isSuccessful()) {
                    throw new IOException("OpenAI HTTP " + httpResp.code() + ": " + responseBodyString);
                }

                JsonObject root = GSON.fromJson(responseBodyString, JsonObject.class);
                JsonElement contentEl = root.getAsJsonArray("choices").get(0).getAsJsonObject().get("message")
                        .getAsJsonObject().get("content");
                String content = contentEl != null ? contentEl.getAsString() : "{}";

                AnalysisOut out = GSON.fromJson(content, AnalysisOut.class);
                if (out == null) {
                    throw new IOException("Failed to parse OpenAI response: " + content);
                }
                
                AnalysisPostProcessor.validateAndFix(out);

                AnalysisResult result = new AnalysisResult();
                result.analysis_id = java.util.UUID.randomUUID().toString();
                
                AnalysisResult.SurfaceMeaning sm = new AnalysisResult.SurfaceMeaning();
                sm.one_line = out.surface != null ? out.surface : "";
                sm.confidence = out.confidence != null ? out.confidence / 100.0 : 0.85;
                sm.evidence = Arrays.asList("GPT 분석");
                result.surface_meaning = sm;

                AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
                hm.one_line = out.hidden != null ? out.hidden : "";
                if (out.translation != null && !out.translation.isEmpty()) {
                    hm.one_line += " (" + out.translation + ")";
                }
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

                com.tokkitalk.analysis.dto.Emotion emo = new com.tokkitalk.analysis.dto.Emotion();
                if (out.emotion != null) {
                    emo.label = out.emotion.label != null ? out.emotion.label : "중립";
                    int intensity = out.emotion.intensity != null ? out.emotion.intensity : 3;
                    emo.valence = intensity - 3;
                    emo.arousal = intensity / 5.0;
                } else {
                    emo.label = "중립";
                    emo.valence = 0;
                    emo.arousal = 0.0;
                }
                emo.politeness_level = "해요체";
                emo.cues = new java.util.HashMap<>();
                emo.confidence = sm.confidence;
                result.emotion = emo;

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
}
