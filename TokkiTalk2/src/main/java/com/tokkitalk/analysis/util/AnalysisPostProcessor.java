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
