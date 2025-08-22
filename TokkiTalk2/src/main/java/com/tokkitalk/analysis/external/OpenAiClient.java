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
                + "톤/스타일 지정:\n"
                + "친근하고 유머러스한 톤으로 답변하고, 여자의 감성을 고려한다,여자는 직접적 조언보다 위로와 공감을 좋아한다. "
                + "성별 고정관념을 조장하지 않도록 주의한다.\n"
                + "실제 상황에서 도움이 되는 실용적인 조언을 제공한다.\n\n"
                + "구체적인 출력 형식:\n"
                + "🔍 **표면적 의미**: (문장 그대로의 뜻)\n"
                + "💡 **숨은 의도**: (실제로 전달하고자 하는 메시지)\n"
                + "❤️ **감정 상태**: (현재 감정 분석)\n"
                + "💬 **추천 대응**: (상황별 적절한 반응)\n\n"
                + "제약 조건:\n"
                + "- 개인차가 있음을 인정하고 일반화하지 않는다\n"
                + "- 건전하고 건설적인 관계 조언에 집중한다\n"
                + "- 성별 갈등을 조장하지 않는다\n\n"
                + "[출력 형식 — JSON만]\n"
                + "{\n"
                + "  \"surface\": \"<표면적 의미 한 줄(문자 그대로)>\",\n"
                + "  \"hidden\": \"<여자어에 숨은 진짜 의도·동기(근거 키워드 1~2개 포함)>\",\n"
                + "  \"emotion\": {\"label\":\"호감|서운함|혼란|기쁨|분노|불안|체념|중립\",\"intensity\":1-5},\n"
                + "  \"translation\": \"<여자어를 남자어/직설 표현으로 짧게 번역>\",\n" +
                "  \"advice\": [\n" +
                "    {\"style\":\"관심표현형\",\"text\":\"...\"},\n" +
                "    {\"style\":\"위로·공감형\",\"text\":\"...\"},\n" +
                "    {\"style\":\"재치있는 응답형\",\"text\":\"...\"},\n" +
                "    {\"style\":\"구체적행동형\",\"text\":\"...\"}\n" +
                "  ],\n" +
                "  \"confidence\": 0-100,\n" +
                "  \"risk_flags\": []\n" +
                "}\n\n" +
                "[엄격한 규칙]\n" +
                "- 반드시 위 JSON만 출력(앞뒤 설명·마크다운 금지).\n" +
                "- 'advice'는 반드시 3~4개로 생성. 서로 다른 스타일/내용, 중복 금지.\n" +
                "- 각 advice.text는 '20~60자'의 구체적인 대화 문장과 행동 가이드 포함. 실용적이고 상세하게.\n" +
                "- 'hidden'에는 근거 키워드 1~2개(예: 역설표현, 응답지연, 선택요구, 질투암시)를 괄호로 포함.\n" +
                "- 확신이 낮으면 confidence<60으로 내리고 risk_flags에 이유 추가(예: [\"맥락부족\"]).\n" +
                "- 성별 고정관념·단정적 일반화 지양. 개인차 전제를 명시.";

        // 기존 FEWSHOTS 대신 사용할 확장된 데이터셋
        final String FEWSHOTS = 
            // 1. 외모/스타일 관련 (3개)
            "예시1_입력: \"나 머리 묶은 게 나아? 푼 게 나아?\"\n" +
            "예시1_출력:{\n" +
            " \"surface\":\"머리 스타일 선호를 묻는 질문\",\n" +
            " \"hidden\":\"칭찬/확인 욕구 (선택요구)\",\n" +
            " \"emotion\":{\"label\":\"호감\",\"intensity\":3},\n" +
            " \"translation\":\"내가 예뻐 보이는지 확인받고 싶어\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"둘 다 예쁜데 오늘은 묶은 게 상큼해 보여 😊 특별한 날이야?\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"너는 어떤 스타일이든 잘 어울려. 네가 편한 걸로 하는 게 최고야.\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"위험한 질문이네 😂 둘 다 예쁘지만 오늘 기분은 어때? 기분에 맞춰 정하자!\"},\n" +
            "  {\"style\":\"구체적행동형\",\"text\":\"묶으면 목선이 예쁘게 보이고, 풀면 부드러워 보여. 오늘 입은 옷이랑 맞춰볼까?\"}\n" +
            " ],\n" +
            " \"confidence\":87,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시2_입력: \"이 옷 어때?\"\n" +
            "예시2_출력:{\n" +
            " \"surface\":\"옷에 대한 의견 요청\",\n" +
            " \"hidden\":\"외모 칭찬과 관심 갈구 (확인요구)\",\n" +
            " \"emotion\":{\"label\":\"기대\",\"intensity\":4},\n" +
            " \"translation\":\"예쁘다고 말해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"어디 가는데? 옷발 받네 😍 오늘은 특별한 날이야?\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"너 센스 좋은 거 알잖아~ 이 옷 정말 너한테 딱이야!\"},\n" +
            "  {\"style\":\"구체적행동형\",\"text\":\"이 색깔이 너 피부톤이랑 완전 어울려. 다음에 같이 쇼핑할까?\"}\n" +
            " ],\n" +
            " \"confidence\":89,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시3_입력: \"살 좀 쪘나?\"\n" +
            "예시3_출력:{\n" +
            " \"surface\":\"체중 증가에 대한 질문\",\n" +
            " \"hidden\":\"위로와 부정적 답변 기대 (안전확인)\",\n" +
            " \"emotion\":{\"label\":\"불안\",\"intensity\":4},\n" +
            " \"translation\":\"아니라고 말해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"전혀! 건강해 보여서 좋은데?\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"어디가? 나는 못 보겠는데 👀\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"너 원래 몸매 좋잖아 뭔 소리야\"}\n" +
            " ],\n" +
            " \"confidence\":92,\"risk_flags\":[]\n" +
            "}\n\n" +

            // 2. 감정 표현/공감 요구 (5개)
            "예시4_입력: \"그냥 들어줘. 답 안 해도 돼.\"\n" +
            "예시4_출력:{\n" +
            " \"surface\":\"답을 요구하지 않음\",\n" +
            " \"hidden\":\"조언보다 공감 원함 (공감요구)\",\n" +
            " \"emotion\":{\"label\":\"답답함\",\"intensity\":4},\n" +
            " \"translation\":\"해결책 말고 내 감정을 먼저 들어줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"그랬구나, 오늘은 네 얘기만 들을게.\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"지금 뭐가 제일 마음에 남았어? 천천히 말해줘.\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"오케이 상담모드 ON. 말할 때까지 조용히 듣기 🤫\"}\n" +
            " ],\n" +
            " \"confidence\":88,\"risk_flags\":[\"조언금지\"]\n" +
            "}\n\n" +

            "예시5_입력: \"오늘 진짜 힘들었어\"\n" +
            "예시5_출력:{\n" +
            " \"surface\":\"하루가 힘들었다는 표현\",\n" +
            " \"hidden\":\"위로와 공감 필요 (감정지지)\",\n" +
            " \"emotion\":{\"label\":\"피곤함\",\"intensity\":4},\n" +
            " \"translation\":\"나를 다독여줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"많이 힘들었구나, 고생했어 😞\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"뭐가 제일 힘들었어? 말하고 싶으면 말해\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"내가 맛있는 거라도 사줄까? 🍕\"}\n" +
            " ],\n" +
            " \"confidence\":85,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시6_입력: \"괜찮아, 별로 안 슬퍼\"\n" +
            "예시6_출력:{\n" +
            " \"surface\":\"괜찮다고 말함\",\n" +
            " \"hidden\":\"실제로는 슬픔 (역설표현)\",\n" +
            " \"emotion\":{\"label\":\"슬픔\",\"intensity\":4},\n" +
            " \"translation\":\"많이 슬픈데 티 안 내려고 해\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"힘들면 괜찮지 않아도 돼. 내가 있잖아\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"진짜 괜찮아? 무리하지 말고 말해줘\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"별로 안 슬프다니까 왜 목소리가... 🥺\"}\n" +
            " ],\n" +
            " \"confidence\":91,\"risk_flags\":[\"감정숨김\"]\n" +
            "}\n\n" +

            // 3. 관계/애정 확인 (4개)
            "예시7_입력: \"요즘 나한테 관심 없는 것 같아\"\n" +
            "예시7_출력:{\n" +
            " \"surface\":\"관심 부족에 대한 지적\",\n" +
            " \"hidden\":\"애정 확인과 관심 요구 (관심갈구)\",\n" +
            " \"emotion\":{\"label\":\"서운함\",\"intensity\":4},\n" +
            " \"translation\":\"나에게 더 관심 가져줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"미안해, 바빠서 그랬어. 너한테 관심 많아\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"그렇게 느꼈구나. 앞으로 더 신경 쓸게\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"관심 없긴! 너 생각 안 하는 날이 어딨어 😤\"}\n" +
            " ],\n" +
            " \"confidence\":88,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시8_입력: \"나 없어도 잘 지낼 것 같아\"\n" +
            "예시8_출력:{\n" +
            " \"surface\":\"독립성에 대한 언급\",\n" +
            " \"hidden\":\"필요성 확인 요구 (존재확인)\",\n" +
            " \"emotion\":{\"label\":\"불안\",\"intensity\":5},\n" +
            " \"translation\":\"내가 너에게 필요하다고 말해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"말도 안 돼, 너 없으면 진짜 안 되는데\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"왜 이런 생각해? 너 때문에 내가 웃잖아\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"잘 지내긴 무슨! 너 없으면 밥도 맛없어 😭\"}\n" +
            " ],\n" +
            " \"confidence\":90,\"risk_flags\":[\"자존감저하\"]\n" +
            "}\n\n" +

            // 4. 일상 대화/소통 (3개)
            "예시9_입력: \"재밌게 놀아~\"\n" +
            "예시9_출력:{\n" +
            " \"surface\":\"즐겁게 놀라고 말함\",\n" +
            " \"hidden\":\"연락·확인 기대 (역설표현)\",\n" +
            " \"emotion\":{\"label\":\"서운함\",\"intensity\":3},\n" +
            " \"translation\":\"놀아도 좋지만 중간에 안부는 원해\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"놀다 생각날 때 짧게 안부 하나만 줘 😉\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"12시 전에 '살아있음' 인증샷 받기? 📸\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"알겠어! 끝나고 바로 연락할게.\"}\n" +
            " ],\n" +
            " \"confidence\":84,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시10_입력: \"연락 안 해도 돼\"\n" +
            "예시10_출력:{\n" +
            " \"surface\":\"연락하지 말라고 함\",\n" +
            " \"hidden\":\"실제로는 연락 원함 (역설표현)\",\n" +
            " \"emotion\":{\"label\":\"서운함\",\"intensity\":4},\n" +
            " \"translation\":\"연락해줬으면 좋겠어\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"그래도 안부는 궁금하잖아 😊\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"안 해도 된다더니 왜 폰 보고 있어? 📱\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"미안해, 내가 연락 못했네. 앞으로 할게\"}\n" +
            " ],\n" +
            " \"confidence\":87,\"risk_flags\":[]\n" +
            "}\n\n" +

            "예시11_입력: \"바쁘지?\"\n" +
            "예시11_출력:{\n" +
            " \"surface\":\"바쁜지 확인하는 질문\",\n" +
            " \"hidden\":\"시간 내달라는 요청 (시간요청)\",\n" +
            " \"emotion\":{\"label\":\"아쉬움\",\"intensity\":3},\n" +
            " \"translation\":\"시간 좀 내줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"조금 바쁘긴 한데 너랑은 시간 낼 수 있어\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"너한테는 안 바빠! 뭐 하고 싶어? 😄\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"바빠도 너 시간은 따로 있어\"}\n" +
            " ],\n" +
            " \"confidence\":85,\"risk_flags\":[]\n" +
            "}\n\n" +

            // 5. 질투/의심 표현 (2개)
            "예시12_입력: \"여자 친구들이랑 재밌었겠다\"\n" +
            "예시12_출력:{\n" +
            " \"surface\":\"친구들과의 시간을 언급\",\n" +
            " \"hidden\":\"질투와 서운함 표현 (질투암시)\",\n" +
            " \"emotion\":{\"label\":\"질투\",\"intensity\":4},\n" +
            " \"translation\":\"다른 여자들보다 나를 더 생각해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"재밌긴 했지만 너 생각 많이 났어\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"너랑 있을 때가 제일 재밌는데 뭔 소리야\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"질투하는 거야? 귀엽네 😏\"}\n" +
            " ],\n" +
            " \"confidence\":89,\"risk_flags\":[\"질투표현\"]\n" +
            "}\n\n" +

            "예시13_입력: \"그 사람 예쁘더라\"\n" +
            "예시13_출력:{\n" +
            " \"surface\":\"타인의 외모에 대한 평가\",\n" +
            " \"hidden\":\"자신과 비교 및 확신 요구 (비교확인)\",\n" +
            " \"emotion\":{\"label\":\"불안\",\"intensity\":4},\n" +
            " \"translation\":\"나랑 비교해서 누가 더 예뻐?\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"예쁘긴 하지만 너만큼은 아니야\"},\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"내 눈에는 네가 제일 예뻐 보이는데?\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"어디서 봤는데? 나는 못 봤네 👀\"}\n" +
            " ],\n" +
            " \"confidence\":86,\"risk_flags\":[]\n" +
            "}\n\n" +

            // 6. 고민/결정 요청 (1개)
            "예시14_입력: \"이거 살까 말까?\"\n" +
            "예시14_출력:{\n" +
            " \"surface\":\"구매 결정에 대한 조언 요청\",\n" +
            " \"hidden\":\"결정 지지와 격려 원함 (결정지지)\",\n" +
            " \"emotion\":{\"label\":\"고민\",\"intensity\":3},\n" +
            " \"translation\":\"사라고 말해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"마음에 든다면 사는 게 어때?\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"고민할 정도면 이미 마음 정한 거 아니야? 😏\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"후회 안 할 것 같으면 질러!\"}\n" +
            " ],\n" +
            " \"confidence\":81,\"risk_flags\":[]\n" +
            "}\n\n" +

            // 7. 칭찬/인정 욕구 (1개)
            "예시15_입력: \"나 요리 잘하지?\"\n" +
            "예시15_출력:{\n" +
            " \"surface\":\"요리 실력에 대한 질문\",\n" +
            " \"hidden\":\"칭찬과 인정 욕구 (칭찬요구)\",\n" +
            " \"emotion\":{\"label\":\"기대\",\"intensity\":4},\n" +
            " \"translation\":\"요리 잘한다고 칭찬해줘\",\n" +
            " \"advice\":[\n" +
            "  {\"style\":\"관심표현형\",\"text\":\"완전 잘해! 맛있어서 깜짝 놀랐어 😋\"},\n" +
            "  {\"style\":\"재치있는 응답형\",\"text\":\"레스토랑 차려도 될 것 같은데? 👨‍🍳\"},\n" +
            "  {\"style\":\"위로·공감형\",\"text\":\"진짜 맛있어, 어떻게 이렇게 잘해?\"}\n" +
            " ],\n" +
            " \"confidence\":90,\"risk_flags\":[]\n" +
            "}\n\n";

        String userPrompt = "입력: \"" + (request != null ? String.valueOf(request.text) : "") + "\"\n아래 JSON 스키마로만 출력하라.";

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


