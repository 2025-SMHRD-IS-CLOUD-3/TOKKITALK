package com.tokkitalk.analysis.dto;

import java.util.List;

public class AnalysisResult {
    public String analysis_id;
    public SurfaceMeaning surface_meaning;
    public HiddenMeaning hidden_meaning;
    public Emotion emotion;
    public ResponseSuggestion response_suggestion;
    public double overall_confidence;

    public static class SurfaceMeaning {
        public String one_line;
        public double confidence;
        public List<String> evidence;
    }

    public static class HiddenMeaning {
        public String one_line;
        public List<LabelScore> intent_labels;
        public List<Flag> risk_flags;
        public double confidence;
    }

    public static class LabelScore {
        public String label;
        public double score;
        public LabelScore(String label, double score) {
            this.label = label; this.score = score;
        }
    }

    public static class Flag {
        public String flag;
        public String reason;
    }
}





