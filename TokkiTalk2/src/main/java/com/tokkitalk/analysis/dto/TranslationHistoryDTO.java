package com.tokkitalk.analysis.dto;

public class TranslationHistoryDTO {
    private String originalText;
    private String translatedText;
    private String analysisResult;
    private String userInput;
    private String imageUrl; // ⭐⭐⭐ 추가된 필드 ⭐⭐⭐

    // 기본 생성자
    public TranslationHistoryDTO() {}

    // 기존 생성자
    public TranslationHistoryDTO(String originalText, String translatedText, String analysisResult, String userInput) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.analysisResult = analysisResult;
        this.userInput = userInput;
    }

    // ⭐⭐⭐ imageUrl이 포함된 새로운 생성자 ⭐⭐⭐
    public TranslationHistoryDTO(String originalText, String translatedText, String analysisResult, String userInput, String imageUrl) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.analysisResult = analysisResult;
        this.userInput = userInput;
        this.imageUrl = imageUrl;
    }

    // Getter와 Setter
    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    // ⭐⭐⭐ 추가된 Getter와 Setter ⭐⭐⭐
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
