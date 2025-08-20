package com.tokkitalk.analysis.dto;

import java.util.List;

public class AnalysisOut {
    public String surface;
    public String hidden;
    public Emotion emotion;                 // {label, intensity}
    public String translation;              // 여자어를 남자어로 번역
    public List<Suggestion> advice;         // [{style, text}, ...]
    public Integer confidence;              // 0~100 (선택)
    public List<String> risk_flags;         // (선택)
    
    public static class Emotion {
        public String label;
        public Integer intensity;
    }
    
    public static class Suggestion {
        public String style;    // "무난·호감형", ...
        public String text;
    }
}
