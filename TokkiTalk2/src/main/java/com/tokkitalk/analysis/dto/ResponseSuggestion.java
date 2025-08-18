package com.tokkitalk.analysis.dto;

import java.util.List;

public class ResponseSuggestion {
    public String tone;
    public String primary;
    public List<String> alternatives;
    public String rationale;
    public double confidence;
}


