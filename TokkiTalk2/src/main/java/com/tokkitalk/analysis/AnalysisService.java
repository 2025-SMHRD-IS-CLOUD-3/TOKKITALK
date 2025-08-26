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

public class AnalysisService {

	private final AnalysisDAO analysisDAO = new AnalysisDAO();
	private final OpenAiClient openAiClient = new OpenAiClient();

	public AnalysisResult analyze(String analysisId, AnalyzeRequest request, String imageBase64, String userId) {
	    AnalysisResult result = null;
	    // Try LLM if configured; fallback to heuristic
	    if (openAiClient.isConfigured()) {
	        try {
	            // OpenAiClient 호출 시 두 개의 파라미터 전달
	            result = openAiClient.analyzeWithLLM(request.text, imageBase64); // 이 부분만 수정
	            result.analysis_id = analysisId; // OpenAiClient에서 생성한 ID를 사용하거나 여기서 설정
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
			sm.one_line = (request.text != null && !request.text.isEmpty())
					? request.text.substring(0, Math.min(30, request.text.length()))
					: "요약 없음";
			sm.confidence = 0.86;
			sm.evidence = Arrays.asList("텍스트 길이", "문장 패턴");
			result.surface_meaning = sm;

			// Hidden meaning
			AnalysisResult.HiddenMeaning hm = new AnalysisResult.HiddenMeaning();
			hm.one_line = "은근한 호감 신호로 보임";
			hm.intent_labels = Arrays.asList(new AnalysisResult.LabelScore("호감테스트", 0.78));
			hm.risk_flags = new ArrayList<>();
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

			// Suggestion - 역설적 상황 특화 휴리스틱 제안
			ResponseSuggestion rs = new ResponseSuggestion();
			rs.tone = request.options != null && request.options.tone != null ? request.options.tone : "친근";

			// 입력 텍스트에 따른 상황별 제안
			String inputText = request.text != null ? request.text.toLowerCase() : "";
			if (inputText.contains("괜찮아") || inputText.contains("괜찮다고")) {
				rs.primary = "괜찮지 않은 것 같은데? 내가 뭘 잘못했는지 말해줘";
				rs.alternatives = Arrays.asList("괜찮다고 하지만 표정이 안 괜찮아 보여. 옆에 있어도 될까?", "괜찮다고 하지만 표정이 안 괜찮아 보여. 옆에 있어도 될까?",
						"괜찮다는 말 믿을 수 없어. 지금 바로 만나자",
						"내가 안 괜찮아,보러 가도 될까?");
			} else if (inputText.contains("알아서") || inputText.contains("판단해")) {
				rs.primary = "알아서 할 게 아니라 같이 해결하자. 뭐가 문제야?";
				rs.alternatives = Arrays.asList("알아서 하기보다는 네 의견이 궁금해. 어떻게 생각해?", "알아서 하기보다는 네 의견이 궁금해. 어떻게 생각해?", "알아서 하라니까 세가지 대안을 줄게, 선택해줄래?",
						"알아서 하는 건 나중에 하고 지금은 너랑 얘기하고 싶어");
			} else if (inputText.contains("신경쓰지마") || inputText.contains("신경 안 써")) {
				rs.primary = "신경 안 쓸 수가 없어. 너한테 관심 많거든";
				rs.alternatives = Arrays.asList("신경 쓰지 말라고? 더 신경 쓸 거야. 뭐가 속상해?", "신경 쓰지 말라고? 더 신경 쓸 거야. 뭐가 속상해?", "신경쓰지 말라는 사람한테 왜 더 신경 쓰이지? 무슨 일이야?",
						"신경 안 쓰기 도전 실패! 계속 생각나는데 어떡해? 😂");
			} else if (inputText.contains("됐어") || inputText.contains("잊어")) {
				rs.primary = "안 된다! 포기하면 안 돼. 뭐가 문제인지 말해봐";
				rs.alternatives = Arrays.asList("됐다고? 전혀 안 됐어. 계속 하자, 내가 도와줄게", "됐다고? 전혀 안 됐어. 계속 하자, 내가 도와줄게", "속상해서 그러는 거지? 그래도 포기하면 아쉬울 것 같은데",
						"안 하기보다는 다른 방법 찾아보자. 같이 고민해볼까?");
			} else if (inputText.contains("혼자") || inputText.contains("혼자서")) {
				rs.primary = "혼자 하지 마, 내가 도와줄게. 뭐부터 시작할까?";
				rs.alternatives = Arrays.asList("혼자 할 거면 나도 옆에서 혼자 할게. 같이 혼자 하자", "혼자 할 거면 나도 옆에서 혼자 할게. 같이 혼자 하자", "혼자 하기 힘든 일이잖아. 나랑 같이 하면 더 쉬울 텐데",
						"혼자 하는 건 서운해. 우리 사이에 뭘 혼자 해?");
			} else if (inputText.contains("상관없어") || inputText.contains("상관없다")) {
				rs.primary = "상관없다는 말투가 상관있어 보이는데? 솔직히 말해줘";
				rs.alternatives = Arrays.asList("너한테는 상관없어도 나한테는 상관있어. 어떻게 생각해?", "너한테는 상관없어도 나한테는 상관있어. 어떻게 생각해?", "상관없다고? 표정 보니까 엄청 상관있는 것 같은데 😏",
						"상관없다고 하니까 더 궁금해져. 정말 괜찮은 거야?");
			} else if (inputText.contains("그냥") || inputText.contains("그런 거야")) {
				rs.primary = "그냥이라는 건 없어. 천천히 말해봐, 시간 많아";
				rs.alternatives = Arrays.asList("그냥이라고 하는 건 뭔가 있다는 뜻이지? 편하게 말해", "그냥이라고 하는 건 뭔가 있다는 뜻이지? 편하게 말해", "그냥이라니까 더 궁금해. 혹시 내가 뭘 잘못했어?",
						"그냥이어도 괜찮아. 말하고 싶을 때까지 기다릴게");
			} else if (inputText.contains("어차피") || inputText.contains("중요하지 않으니까")) {
				rs.primary = "무슨 소리야, 네 말은 항상 들어줘. 뭔데 말해봐";
				rs.alternatives = Arrays.asList("안 들어준다고? 네 말이 제일 중요한데. 말해줘", "안 들어준다고? 네 말이 제일 중요한데. 말해줘", "말도 안 돼! 너는 나한테 제일 중요한 사람이야",
						"중요하지 않다고? 너 때문에 내가 얼마나 행복한지 알아?");
			} else if (inputText.contains("생각해볼게") || inputText.contains("고민해볼게")) {
				rs.primary = "천천히 생각해도 돼. 부담 갖지 말고";
				rs.alternatives = Arrays.asList("다른 방법도 있으니까 편한 대로 해", "다른 방법도 있으니까 편한 대로 해", "솔직하게 말해도 괜찮아. 무리하지 마", "고민되는 게 있으면 얘기해줘");
			} else if (inputText.contains("다음에") || inputText.contains("나중에")) {
				rs.primary = "알겠어, 네가 하고 싶을 때 하자";
				rs.alternatives = Arrays.asList("괜찮아, 급한 게 아니니까 천천히", "괜찮아, 급한 게 아니니까 천천히", "다음에 꼭 하자. 잊지 않을게", "지금 바쁘구나. 시간 날 때 편하게");
			} else if (inputText.contains("별로 안 중요해") || inputText.contains("중요하지 않아")) {
				rs.primary = "중요하지 않은 것 같지 않은데? 솔직히 말해줘";
				rs.alternatives = Arrays.asList("나한테는 중요해. 네가 신경쓰는 일이니까", "나한테는 중요해. 네가 신경쓰는 일이니까", "중요한 일이면 같이 생각해보자",
						"중요하다고 말해도 괜찮아. 나는 이해해");
			} else if (inputText.contains("누가 더 예뻐") || inputText.contains("비교")) {
				rs.primary = "비교 자체가 말이 안 돼. 너는 유일해";
				rs.alternatives = Arrays.asList("둘 다 각자 매력이 있지만 내 마음은 정해져 있어", "둘 다 각자 매력이 있지만 내 마음은 정해져 있어", "친구든 누구든 너만큼 예쁜 사람은 없어",
						"왜 이런 질문해? 네가 제일인 거 모르나? 😊");
			} else if (inputText.contains("다른 여자") || inputText.contains("예쁘다고")) {
				rs.primary = "다른 여자는 관심 없어. 너만 보여";
				rs.alternatives = Arrays.asList("말해도 된다고? 그런 함정에 안 걸려 😏", "말해도 된다고? 그런 함정에 안 걸려 😏", "예쁜 사람 많지만 너 같은 사람은 없어",
						"왜 이런 질문해? 네가 최고인데 다른 사람이 왜 필요해?");
			} else if (inputText.contains("전 여친") || inputText.contains("이전")) {
				rs.primary = "비교할 수 없어. 너는 완전히 다른 차원이야";
				rs.alternatives = Arrays.asList("과거는 과거고, 지금 내 옆에 있는 건 너야", "과거는 과거고, 지금 내 옆에 있는 건 너야", "비교 자체가 의미 없어. 너는 대체 불가능해",
						"왜 과거 얘기를? 지금이 제일 행복한데");
			} else if (inputText.contains("시큰둥") || inputText.contains("관심 없어")) {
				rs.primary = "시큰둥하긴! 너한테 늘 관심 많아";
				rs.alternatives = Arrays.asList("그렇게 느꼈다면 미안해. 앞으로 더 표현할게", "그렇게 느꼈다면 미안해. 앞으로 더 표현할게", "시큰둥할 리 없잖아. 너 없으면 안 되는데",
						"요즘 바빠서 그랬나? 주말에 시간 내서 데이트하자");
			} else if (inputText.contains("잘 살") || inputText.contains("없어도")) {
				rs.primary = "말도 안 돼! 너 없으면 진짜 못 살아";
				rs.alternatives = Arrays.asList("잘 살기는 무슨, 너 때문에 내가 행복한 건데", "잘 살기는 무슨, 너 때문에 내가 행복한 건데", "하루도 너 생각 안 하는 날이 없는데 무슨 소리야",
						"없어도 된다는 생각 버려. 평생 함께 할 거야");
			} else if (inputText.contains("좋은 사람") || inputText.contains("나보다")) {
				rs.primary = "좋은 사람은 많아도 너 같은 사람은 없어";
				rs.alternatives = Arrays.asList("내가 너를 선택한 이유가 있어. 너는 특별해", "내가 너를 선택한 이유가 있어. 너는 특별해", "다른 사람과 비교할 수 없어. 너는 유일해",
						"더 좋은 사람이 있어도 난 너만 원해");
			} else if (inputText.contains("연락이 뜸해") || inputText.contains("연락 안 해")) {
				rs.primary = "미안해, 바빠서 그랬어. 앞으로 더 자주 할게";
				rs.alternatives = Arrays.asList("맞네, 앞으로 매일 안부 인사하자", "맞네, 앞으로 매일 안부 인사하자", "연락 못한 사이에도 너 생각 많이 했어",
						"아침저녁으로 안부 확인하는 습관 만들자");
			} else if (inputText.contains("혼자 있는 시간") || inputText.contains("혼자 보내는")) {
				rs.primary = "그럼 같이 있자! 지금 시간 있어?";
				rs.alternatives = Arrays.asList("외롭겠네. 이번 주말에 같이 뭐 할까?", "외롭겠네. 이번 주말에 같이 뭐 할까?", "혼자 있지 말고 언제든 불러. 바로 간게",
						"혼자 있으면 뭐 해? 재미있는 거 같이 찾아보자");
			} else if (inputText.contains("커플로") || inputText.contains("다들 나가")) {
				rs.primary = "그럼 우리도 나가자! 어디 가고 싶어?";
				rs.alternatives = Arrays.asList("커플로 나간다고? 우리가 제일 멋진 커플이 되자", "커플로 나간다고? 우리가 제일 멋진 커플이 되자", "우리도 커플 데이트 코스 찾아보자",
						"남들 따라하지 말고 우리만의 특별한 데이트 하자");
			} else if (inputText.contains("화 안 났어") || inputText.contains("화 안 났다")) {
				rs.primary = "화 안 났다는 말투가 화난 것 같은데? 뭐가 속상해?";
				rs.alternatives = Arrays.asList("화 안 났다고 해도 내가 뭔가 잘못한 것 같아. 미안해", "화 안 났다고 해도 내가 뭔가 잘못한 것 같아. 미안해", "화 났든 안 났든 일단 얘기해보자. 뭐가 문제야?",
						"화 안 났다니까 더 궁금해. 진짜 괜찮은 거 맞아?");
			} else if (inputText.contains("별일 아니야") || inputText.contains("대수롭지 않아")) {
				rs.primary = "별일 아니라고? 너한테 일어난 일은 다 중요해";
				rs.alternatives = Arrays.asList("별일 아니어도 궁금해. 무슨 일인지 말해줘", "별일 아니어도 궁금해. 무슨 일인지 말해줘", "별일 아니어도 계속 신경 쓰일 것 같은데",
						"별일 아니라는 말이 별일 같다는 뜻 아니야?");
			} else if (inputText.contains("기분이 안 좋아") || inputText.contains("기분 나빠")) {
				rs.primary = "그냥 기분 나쁠 리 없어. 무슨 일 있었어?";
				rs.alternatives = Arrays.asList("기분 안 좋구나. 무엇 때문인지 얘기해줄래?", "기분 안 좋구나. 무엇 때문인지 얘기해줄래?", "기분 안 좋은 날이 있지. 내가 옆에 있을게",
						"기분 나쁜 이유 찾아서 같이 해결해보자");
			} else if (inputText.contains("우리 사이") || inputText.contains("관계")) {
				rs.primary = "완전 좋아! 너와 함께여서 매일 행복해";
				rs.alternatives = Arrays.asList("지금도 좋지만 앞으로 더 좋아질 것 같아", "지금도 좋지만 앞으로 더 좋아질 것 같아", "우리 사이는 특별해. 이런 관계 처음이야",
						"지금 완벽해. 계속 이렇게 함께 하고 싶어");
			} else {
				// 기본 제안들
				rs.primary = "완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?";
				rs.alternatives = Arrays.asList("많이 힘들었구나, 고생했어 😞 내가 옆에서 쉬게 해줄게", "위험한 질문이네 😂 난 이거 한 표! 기분에 맞춰 정하자!",
						"내가 맛있는 거라도 사줄까? 🍕 오늘은 특별히 예쁘니까");
			}

			rs.rationale = "역설적 상황 특화 분석";
			rs.confidence = 0.85;
			result.response_suggestion = rs;

			// Advice 배열도 추가 (스타일 정보 포함)
			result.advice = new ArrayList<>();
			AnalysisResult.AdviceItem item1 = new AnalysisResult.AdviceItem();
			item1.style = "적극대응형";
			item1.text = rs.primary;
			result.advice.add(item1);

			AnalysisResult.AdviceItem item2 = new AnalysisResult.AdviceItem();
			item2.style = "공감접근형";
			item2.text = rs.alternatives.get(0);
			result.advice.add(item2);

			AnalysisResult.AdviceItem item3 = new AnalysisResult.AdviceItem();
			item3.style = "재치있는 응답형";
			item3.text = rs.alternatives.get(1);
			result.advice.add(item3);

			AnalysisResult.AdviceItem item4 = new AnalysisResult.AdviceItem();
			item4.style = "구체적행동형";
			item4.text = rs.alternatives.get(2);
			result.advice.add(item4);

			result.overall_confidence = 0.80;
		}

		// Persist minimal record
		analysisDAO.saveResult(result, userId);
		return result;
	}

	// 아래에 추가된 메서드들

	// 1. DeleteAnalysisServlet에서 호출하는 메서드
	public boolean deleteAnalysis(String id) {
		try {
			analysisDAO.delete(id);
			return true;
		} catch (Exception e) {
			// 오류 처리 로직
			e.printStackTrace();
			return false;
		}
	}

	// 2. FeedbackServlet에서 호출하는 메서드
	public void saveFeedback(FeedbackRequest feedbackRequest) {
		analysisDAO.saveFeedback(feedbackRequest);
	}

	// 3. SuggestServlet에서 호출하는 메서드
	public SuggestResult regenerateSuggestion(SuggestRequest suggestRequest) {
		SuggestResult result = null;
		// LLM으로 새로운 제안을 생성하는 로직을 추가합니다.
		if (openAiClient.isConfigured()) {
			try {
				// suggestRequest를 사용하여 새로운 제안을 요청합니다.
				// 이 부분은 OpenAiClient의 메서드 시그니처에 맞게 수정해야 합니다.
				// 예시: result = openAiClient.regenerateSuggestionWithLLM(suggestRequest);
				// 현재 코드에는 해당 메서드가 없으므로, 임시로 null을 반환합니다.
				// TODO: OpenAiClient에 regenerateSuggestionWithLLM 메서드를 추가하세요.
			} catch (Exception e) {
				e.printStackTrace();
				// 오류 발생 시 null 반환
			}
		}
		return result;
	}
}