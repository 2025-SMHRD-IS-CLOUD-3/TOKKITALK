package com.tokkitalk.analysis.dto;

import java.util.Map;

public class Emotion {
    public String label;
    public int valence;
    public double arousal;
    public String politeness_level;
    public Map<String, Object> cues;
    public double confidence;
}


