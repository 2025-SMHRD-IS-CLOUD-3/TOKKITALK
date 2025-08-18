package com.tokkitalk.analysis.dto;

public class AnalyzeRequest {
    public String input_type; // text | image
    public String text;
    public String image_base64;
    public Options options;

    public static class Options {
        public Integer turns_used;
        public String tone;
    }
}





