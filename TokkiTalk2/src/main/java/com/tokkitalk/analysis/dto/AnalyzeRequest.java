package com.tokkitalk.analysis.dto;

public class AnalyzeRequest {
    public String input_type; // text | image
    public String text;
    public String imageBase64;
    public Options options;

    public static class Options {
        public Integer turns_used;
        public String tone;
    }

    // 이 두 메서드를 추가합니다.
    public String getText() {
        return this.text;
    }

    public String getImageBase64() {
        return this.imageBase64;
    }
}