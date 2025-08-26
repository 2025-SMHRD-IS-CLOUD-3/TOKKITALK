package com.tokkitalk.analysis.store;

import java.sql.Timestamp;

/**
 * ANALYSIS 테이블의 데이터를 담는 DTO 클래스
 */
public class AnalysisRecord {
    
    private long analysisId;
    private String userId;
    private String inputType;
    private String text;
    private String imageBase64;
    private String analysisResult;
    private String imageUrl;
    private String tone;
    private String suggestions;
    private Timestamp createdAt;

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public String getUserId() {
        return userId;
    }

    // 🚨 이 부분을 수정했습니다. 매개변수 타입을 String으로 변경.
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "AnalysisRecord{" +
                "analysisId=" + analysisId +
                ", userId='" + userId + '\'' +
                ", inputType='" + inputType + '\'' +
                ", text='" + (text != null ? text.substring(0, Math.min(text.length(), 20)) + "..." : "null") + '\'' +
                ", imageBase64 is " + (imageBase64 != null ? "present" : "absent") +
                ", analysisResult='" + (analysisResult != null ? "present" : "absent") + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}