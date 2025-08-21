package com.tokkitalk.analysis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.tokkitalk.analysis.dto.AnalysisOut;
import com.tokkitalk.analysis.dto.AnalysisOut.Suggestion;

public class AnalysisPostProcessor {
    
    /**
     * 제안 개수를 랜덤으로 선택 (3~4개)
     */
    public static List<Suggestion> pickRandom(List<Suggestion> in) {
        if (in == null) in = new ArrayList<>();
        Collections.shuffle(in);
        int max = Math.min(4, in.size());
        int min = Math.min(3, max);                 // 최소 3개 보장
        int n = (max >= 3) ? (new Random().nextInt(max - min + 1) + min) : max;
        return in.stream().limit(n).collect(Collectors.toList());
    }
    
    /**
     * 최소 3개 보장 (부족하면 휴리스틱으로 보충)
     */
    public static List<Suggestion> fallbackIfTooFew(List<Suggestion> in, AnalysisOut a) {
        if (in == null) in = new ArrayList<>();
        while (in.size() < 3) {                     // 최소 3개 보장
            in.addAll(makeHeuristic(a));              // 휴리스틱로 보충
            in = in.stream().distinct().limit(4).collect(Collectors.toList());
        }
        return in;
    }
    
    /**
     * 휴리스틱 제안 생성 - 여자어 번역기에 맞는 제안
     */
    public static List<Suggestion> makeHeuristic(AnalysisOut a) {
        String mood = (a != null && a.emotion != null) ? a.emotion.label : "중립";
        String surf = (a != null && a.surface != null) ? a.surface : "";
        
        // 상황에 따른 다양한 제안 생성
        List<Suggestion> suggestions = new ArrayList<>();
        
        // 확장된 데이터셋 기반의 다양한 제안 (더 구체적이고 긴 제안들)
        suggestions.add(s("관심표현형", "완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?"));
        suggestions.add(s("위로·공감형", "많이 힘들었구나, 고생했어 😞 내가 옆에서 쉬게 해줄게"));
        suggestions.add(s("재치있는 응답형", "위험한 질문이네 😂 난 이거 한 표! 기분에 맞춰 정하자!"));
        suggestions.add(s("구체적행동형", "내가 맛있는 거라도 사줄까? 🍕 오늘은 특별히 예쁘니까"));
        suggestions.add(s("위로·공감형", "힘들면 괜찮지 않아도 돼. 내가 있잖아 언제든 말해줘"));
        suggestions.add(s("재치있는 응답형", "오늘은 내가 다 할게, 넌 쉬어 😴 내가 옆에서 돌봐줄게"));
        
        // 확장된 데이터셋 기반 감정별 추가 제안
        if (mood != null && !mood.equals("중립")) {
            if (mood.contains("고민") || mood.contains("불안")) {
                suggestions.add(s("위로·공감형", "괜찮아, 천천히 생각해봐. 내가 기다릴게 😊"));
                suggestions.add(s("관심표현형", "그런 마음이 드는 게 당연해. 내가 이해해 💕"));
            } else if (mood.contains("기대") || mood.contains("호기심")) {
                suggestions.add(s("관심표현형", "무엇을 기대하고 있는지 궁금해! 말해줘 😊"));
                suggestions.add(s("재치있는 응답형", "나도 함께 기대하고 있어! 어떤 일이야? ✨"));
            } else if (mood.contains("피곤") || mood.contains("스트레스")) {
                suggestions.add(s("위로·공감형", "편하게 쉬어. 내가 방해하지 않을게 😴"));
                suggestions.add(s("관심표현형", "힘들 때는 내가 있어. 언제든 말해줘 💪"));
            } else if (mood.contains("궁금") || mood.contains("호기심")) {
                suggestions.add(s("관심표현형", "궁금한 게 있으면 언제든 물어봐! 내가 알려줄게 😊"));
                suggestions.add(s("재치있는 응답형", "함께 알아보자! 뭐가 궁금한지 말해줘 🔍"));
            } else if (mood.contains("서운함") || mood.contains("슬픔")) {
                suggestions.add(s("위로·공감형", "미안해, 내가 잘못했어. 화 풀어"));
                suggestions.add(s("관심표현형", "뭐가 속상했어? 말해줘"));
            } else if (mood.contains("질투") || mood.contains("불안")) {
                suggestions.add(s("위로·공감형", "재밌긴 했지만 너 생각 많이 났어"));
                suggestions.add(s("재치있는 응답형", "질투하는 거야? 귀엽네 😏"));
            } else if (mood.contains("분노") || mood.contains("화남")) {
                suggestions.add(s("위로·공감형", "화났구나 😅 내가 어떻게 할까?"));
                suggestions.add(s("관심표현형", "진짜 괜찮아? 무리하지 말고 말해줘"));
            }
        }
        
        // 역설적 상황 특화 제안들 추가
        if (surf != null && !surf.isEmpty()) {
            if (surf.contains("괜찮아") || surf.contains("괜찮다고")) {
                suggestions.add(s("적극대응형", "괜찮지 않은 것 같은데? 내가 뭘 잘못했는지 말해줘"));
                suggestions.add(s("공감접근형", "괜찮다고 하지만 표정이 안 괜찮아 보여. 옆에 있어도 될까?"));
                suggestions.add(s("직접행동형", "괜찮다는 말 믿을 수 없어. 지금 바로 만나자"));
                suggestions.add(s("유머완화형", "괜찮다는 여자는 괜찮지 않다는 법칙 발동! 뭐가 속상해? 😅"));
            } else if (surf.contains("알아서") || surf.contains("판단해")) {
                suggestions.add(s("적극해결형", "알아서 할 게 아니라 같이 해결하자. 뭐가 문제야?"));
                suggestions.add(s("관심집중형", "알아서 하기보다는 네 의견이 궁금해. 어떻게 생각해?"));
                suggestions.add(s("화해시도형", "알아서 하라니까 화난 거 같은데, 내가 뭘 잘못했어?"));
                suggestions.add(s("끈질한대화형", "알아서 하는 건 나중에 하고 지금은 너랑 얘기하고 싶어"));
            } else if (surf.contains("신경쓰지마") || surf.contains("신경 안 써")) {
                suggestions.add(s("반대행동형", "신경 안 쓸 수가 없어. 너한테 관심 많거든"));
                suggestions.add(s("지속관심형", "신경 쓰지 말라고? 더 신경 쓸 거야. 뭐가 속상해?"));
                suggestions.add(s("솔직대응형", "신경쓰지 말라는 사람한테 왜 더 신경 쓰이지? 무슨 일이야?"));
                suggestions.add(s("유머적접근형", "신경 안 쓰기 도전 실패! 계속 생각나는데 어떡해? 😂"));
            } else if (surf.contains("됐어") || surf.contains("잊어")) {
                suggestions.add(s("적극만류형", "안 된다! 포기하면 안 돼. 뭐가 문제인지 말해봐"));
                suggestions.add(s("끈질한설득형", "됐다고? 전혀 안 됐어. 계속 하자, 내가 도와줄게"));
                suggestions.add(s("감정공감형", "화나서 그러는 거지? 그래도 포기하면 아쉬울 것 같은데"));
                suggestions.add(s("대안제시형", "안 하기보다는 다른 방법 찾아보자. 같이 고민해볼까?"));
            } else if (surf.contains("혼자") || surf.contains("혼자서")) {
                suggestions.add(s("적극참여형", "혼자 하지 마, 내가 도와줄게. 뭐부터 시작할까?"));
                suggestions.add(s("강제동참형", "혼자 할 거면 나도 옆에서 혼자 할게. 같이 혼자 하자"));
                suggestions.add(s("부담덜기형", "혼자 하기 힘든 일이잖아. 나랑 같이 하면 더 쉬울 텐데"));
                suggestions.add(s("관계강조형", "혼자 하는 건 서운해. 우리 사이에 뭘 혼자 해?"));
            } else if (surf.contains("상관없어") || surf.contains("상관없다")) {
                suggestions.add(s("진심파악형", "상관없다는 말투가 상관있어 보이는데? 솔직히 말해줘"));
                suggestions.add(s("관심표현형", "너한테는 상관없어도 나한테는 상관있어. 어떻게 생각해?"));
                suggestions.add(s("반대추측형", "상관없다고? 표정 보니까 엄청 상관있는 것 같은데 😏"));
                suggestions.add(s("지속질문형", "상관없다고 하니까 더 궁금해져. 정말 괜찮은 거야?"));
            } else if (surf.contains("그냥") || surf.contains("그런 거야")) {
                suggestions.add(s("끈질한추궁형", "그냥이라는 건 없어. 천천히 말해봐, 시간 많아"));
                suggestions.add(s("편안한분위기형", "그냥이라고 하는 건 뭔가 있다는 뜻이지? 편하게 말해"));
                suggestions.add(s("추측접근형", "그냥이라니까 더 궁금해. 혹시 내가 뭘 잘못했어?"));
                suggestions.add(s("인내심표현형", "그냥이어도 괜찮아. 말하고 싶을 때까지 기다릴게"));
            } else if (surf.contains("어차피") || surf.contains("중요하지 않으니까")) {
                suggestions.add(s("즉시반박형", "무슨 소리야, 네 말은 항상 들어줘. 뭔데 말해봐"));
                suggestions.add(s("진심표현형", "안 들어준다고? 네 말이 제일 중요한데. 말해줘"));
                suggestions.add(s("강력부정형", "말도 안 돼! 너는 나한테 제일 중요한 사람이야"));
                suggestions.add(s("구체적증명형", "중요하지 않다고? 너 때문에 내가 얼마나 행복한지 알아?"));
            } else if (surf.contains("생각해볼게") || surf.contains("고민해볼게")) {
                suggestions.add(s("압박완화형", "천천히 생각해도 돼. 부담 갖지 말고"));
                suggestions.add(s("대안제시형", "다른 방법도 있으니까 편한 대로 해"));
                suggestions.add(s("솔직유도형", "솔직하게 말해도 괜찮아. 무리하지 마"));
                suggestions.add(s("이해표현형", "고민되는 게 있으면 얘기해줘"));
            } else if (surf.contains("다음에") || surf.contains("나중에")) {
                suggestions.add(s("이해수용형", "알겠어, 네가 하고 싶을 때 하자"));
                suggestions.add(s("부담해소형", "괜찮아, 급한 게 아니니까 천천히"));
                suggestions.add(s("관심지속형", "다음에 꼭 하자. 잊지 않을게"));
                suggestions.add(s("배려표현형", "지금 바쁘구나. 시간 날 때 편하게"));
            } else if (surf.contains("별로 안 중요해") || surf.contains("중요하지 않아")) {
                suggestions.add(s("진심파악형", "중요하지 않은 것 같지 않은데? 솔직히 말해줘"));
                suggestions.add(s("중요성인정형", "나한테는 중요해. 네가 신경쓰는 일이니까"));
                suggestions.add(s("지지표현형", "중요한 일이면 같이 생각해보자"));
                suggestions.add(s("안전감제공형", "중요하다고 말해도 괜찮아. 나는 이해해"));
            } else if (surf.contains("누가 더 예뻐") || surf.contains("비교")) {
                suggestions.add(s("확신주기형", "비교 자체가 말이 안 돼. 너는 유일해"));
                suggestions.add(s("위험회피형", "둘 다 각자 매력이 있지만 내 마음은 정해져 있어"));
                suggestions.add(s("애정표현형", "친구든 누구든 너만큼 예쁜 사람은 없어"));
                suggestions.add(s("질문돌리기형", "왜 이런 질문해? 네가 제일인 거 모르나? 😊"));
            } else if (surf.contains("다른 여자") || surf.contains("예쁘다고")) {
                suggestions.add(s("안전대답형", "다른 여자는 관심 없어. 너만 보여"));
                suggestions.add(s("함정인식형", "말해도 된다고? 그런 함정에 안 걸려 😏"));
                suggestions.add(s("애정확신형", "예쁜 사람 많지만 너 같은 사람은 없어"));
                suggestions.add(s("질문반박형", "왜 이런 질문해? 네가 최고인데 다른 사람이 왜 필요해?"));
            } else if (surf.contains("전 여친") || surf.contains("이전")) {
                suggestions.add(s("과거차단형", "비교할 수 없어. 너는 완전히 다른 차원이야"));
                suggestions.add(s("현재집중형", "과거는 과거고, 지금 내 옆에 있는 건 너야"));
                suggestions.add(s("유일성강조형", "비교 자체가 의미 없어. 너는 대체 불가능해"));
                suggestions.add(s("질문돌리기형", "왜 과거 얘기를? 지금이 제일 행복한데"));
            } else if (surf.contains("시큰둥") || surf.contains("관심 없어")) {
                suggestions.add(s("즉시부정형", "시큰둥하긴! 너한테 늘 관심 많아"));
                suggestions.add(s("행동개선형", "그렇게 느꼈다면 미안해. 앞으로 더 표현할게"));
                suggestions.add(s("애정재확인형", "시큰둥할 리 없잖아. 너 없으면 안 되는데"));
                suggestions.add(s("구체적계획형", "요즘 바빠서 그랬나? 주말에 시간 내서 데이트하자"));
            } else if (surf.contains("잘 살") || surf.contains("없어도")) {
                suggestions.add(s("강력거부형", "말도 안 돼! 너 없으면 진짜 못 살아"));
                suggestions.add(s("구체적설명형", "잘 살기는 무슨, 너 때문에 내가 행복한 건데"));
                suggestions.add(s("일상증명형", "하루도 너 생각 안 하는 날이 없는데 무슨 소리야"));
                suggestions.add(s("미래약속형", "없어도 된다는 생각 버려. 평생 함께 할 거야"));
            } else if (surf.contains("좋은 사람") || surf.contains("나보다")) {
                suggestions.add(s("유일성강조형", "좋은 사람은 많아도 너 같은 사람은 없어"));
                suggestions.add(s("선택확신형", "내가 너를 선택한 이유가 있어. 너는 특별해"));
                suggestions.add(s("비교거부형", "다른 사람과 비교할 수 없어. 너는 유일해"));
                suggestions.add(s("미래확약형", "더 좋은 사람이 있어도 난 너만 원해"));
            } else if (surf.contains("연락이 뜸해") || surf.contains("연락 안 해")) {
                suggestions.add(s("즉시개선형", "미안해, 바빠서 그랬어. 앞으로 더 자주 할게"));
                suggestions.add(s("패턴개선형", "맞네, 앞으로 매일 안부 인사하자"));
                suggestions.add(s("관심표현형", "연락 못한 사이에도 너 생각 많이 했어"));
                suggestions.add(s("약속제시형", "아침저녁으로 안부 확인하는 습관 만들자"));
            } else if (surf.contains("혼자 있는 시간") || surf.contains("혼자 보내는")) {
                suggestions.add(s("즉시제안형", "그럼 같이 있자! 지금 시간 있어?"));
                suggestions.add(s("계획제시형", "외롭겠네. 이번 주말에 같이 뭐 할까?"));
                suggestions.add(s("동반의지형", "혼자 있지 말고 언제든 불러. 바로 간게"));
                suggestions.add(s("관심표현형", "혼자 있으면 뭐 해? 재미있는 거 같이 찾아보자"));
            } else if (surf.contains("커플로") || surf.contains("다들 나가")) {
                suggestions.add(s("즉시응답형", "그럼 우리도 나가자! 어디 가고 싶어?"));
                suggestions.add(s("특별계획형", "커플로 나간다고? 우리가 제일 멋진 커플이 되자"));
                suggestions.add(s("동참의지형", "우리도 커플 데이트 코스 찾아보자"));
                suggestions.add(s("독특함강조형", "남들 따라하지 말고 우리만의 특별한 데이트 하자"));
            } else if (surf.contains("화 안 났어") || surf.contains("화 안 났다")) {
                suggestions.add(s("진실파악형", "화 안 났다는 말투가 화난 것 같은데? 뭐가 속상해?"));
                suggestions.add(s("선제사과형", "화 안 났다고 해도 내가 뭔가 잘못한 것 같아. 미안해"));
                suggestions.add(s("관심집중형", "화 났든 안 났든 일단 얘기해보자. 뭐가 문제야?"));
                suggestions.add(s("끈질한관심형", "화 안 났다니까 더 궁금해. 진짜 괜찮은 거 맞아?"));
            } else if (surf.contains("별일 아니야") || surf.contains("대수롭지 않아")) {
                suggestions.add(s("중요성인정형", "별일 아니라고? 너한테 일어난 일은 다 중요해"));
                suggestions.add(s("관심표현형", "별일 아니어도 궁금해. 무슨 일인지 말해줘"));
                suggestions.add(s("지속관심형", "별일 아니라고 해도 계속 신경 쓰일 것 같은데"));
                suggestions.add(s("진심파악형", "별일 아니라는 말이 별일 같다는 뜻 아니야?"));
            } else if (surf.contains("기분이 안 좋아") || surf.contains("기분 나빠")) {
                suggestions.add(s("원인탐색형", "그냥 기분 나쁠 리 없어. 무슨 일 있었어?"));
                suggestions.add(s("공감접근형", "기분 안 좋구나. 무엇 때문인지 얘기해줄래?"));
                suggestions.add(s("위로제공형", "기분 안 좋은 날이 있지. 내가 옆에 있을게"));
                suggestions.add(s("해결의지형", "기분 나쁜 이유 찾아서 같이 해결해보자"));
            } else if (surf.contains("우리 사이") || surf.contains("관계")) {
                suggestions.add(s("긍정확신형", "완전 좋아! 너와 함께여서 매일 행복해"));
                suggestions.add(s("발전의지형", "지금도 좋지만 앞으로 더 좋아질 것 같아"));
                suggestions.add(s("특별강조형", "우리 사이는 특별해. 이런 관계 처음이야"));
                suggestions.add(s("미래지향형", "지금 완벽해. 계속 이렇게 함께 하고 싶어"));
            }
        }
        
        return suggestions;
    }
    
    private static Suggestion s(String style, String text) {
        Suggestion r = new Suggestion();
        r.style = style;
        r.text = text;
        return r;
    }
    
    /**
     * AnalysisOut 유효성 검사 및 보정
     */
    public static void validateAndFix(AnalysisOut out) {
        // 감정 기본값 보정
        if (out.emotion == null || out.emotion.intensity == null) {
            AnalysisOut.Emotion e = new AnalysisOut.Emotion();
            e.label = "중립";
            e.intensity = 2;
            out.emotion = e;
        }
        
        // 제안 개수 랜덤(3~4) + 최소 3개 보장
        List<Suggestion> picked = pickRandom(out.advice);
        picked = fallbackIfTooFew(picked, out);
        out.advice = picked;
        
        // 신뢰도 기본값
        if (out.confidence == null) {
            out.confidence = 75;
        }
        
        // risk_flags 기본값
        if (out.risk_flags == null) {
            out.risk_flags = new ArrayList<>();
        }
    }
}
