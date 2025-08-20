package com.tokkitalk.analysis.store;

/**
 * Persistence DTO that mirrors the ANALYSIS table.
 */
public class AnalysisRecord {
    public String analysisId;
    public String inputType;
    public String text;
    public String imageUrl;
    public String tone;
    public String analysisResult; // JSON string
    public String suggestions;    // JSON string
}


