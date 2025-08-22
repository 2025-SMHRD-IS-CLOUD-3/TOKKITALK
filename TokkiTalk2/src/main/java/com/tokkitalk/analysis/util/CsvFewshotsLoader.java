package com.tokkitalk.analysis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 파일에서 fewshots 데이터를 읽어서 JSON 형식으로 변환하는 유틸리티 클래스
 */
public class CsvFewshotsLoader {
    
    /**
     * CSV 파일을 읽어서 fewshots 문자열로 변환
     * @return fewshots 문자열
     */
    public static String loadFewshotsFromCsv() {
        StringBuilder fewshots = new StringBuilder();
        
        try (InputStream is = CsvFewshotsLoader.class.getClassLoader().getResourceAsStream("fewshots.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 헤더 스킵
                }
                
                String[] fields = parseCsvLine(line);
                if (fields.length >= 19) { // 최소 필드 수 확인
                    String fewshot = convertToFewshot(fields);
                    fewshots.append(fewshot).append("\n\n");
                }
            }
            
        } catch (IOException e) {
            System.err.println("CSV 파일 읽기 실패: " + e.getMessage());
            return ""; // 실패 시 빈 문자열 반환
        }
        
        return fewshots.toString();
    }
    
    /**
     * CSV 라인을 파싱 (쉼표로 구분, 따옴표 처리)
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // 마지막 필드 추가
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * CSV 필드를 fewshot 형식으로 변환
     */
    private static String convertToFewshot(String[] fields) {
        String id = fields[0];
        String inputText = fields[2];
        String surfaceMeaning = fields[3];
        String hiddenMeaning = fields[4];
        String emotionLabel = fields[5];
        String emotionIntensity = fields[6];
        String translation = fields[7];
        
        // advice 필드들 (8-15번 인덱스)
        StringBuilder adviceJson = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int styleIndex = 8 + i * 2;
            int textIndex = 9 + i * 2;
            
            if (styleIndex < fields.length && textIndex < fields.length && 
                !fields[styleIndex].isEmpty() && !fields[textIndex].isEmpty()) {
                
                if (adviceJson.length() > 0) {
                    adviceJson.append(",\n");
                }
                
                adviceJson.append("  {\"style\":\"").append(escapeJson(fields[styleIndex]))
                         .append("\",\"text\":\"").append(escapeJson(fields[textIndex])).append("\"}");
            }
        }
        
        String confidence = fields.length > 16 ? fields[16] : "85";
        String riskFlags = fields.length > 17 ? fields[17] : "";
        
        // risk_flags 처리
        String riskFlagsJson = "[]";
        if (!riskFlags.isEmpty()) {
            String[] flags = riskFlags.split(",");
            if (flags.length > 0) {
                StringBuilder flagsJson = new StringBuilder("[");
                for (int i = 0; i < flags.length; i++) {
                    if (i > 0) flagsJson.append(",");
                    flagsJson.append("\"").append(escapeJson(flags[i].trim())).append("\"");
                }
                flagsJson.append("]");
                riskFlagsJson = flagsJson.toString();
            }
        }
        
        return String.format("예시%s_입력: \"%s\"\n" +
                           "예시%s_출력:{\n" +
                           " \"surface\":\"%s\",\n" +
                           " \"hidden\":\"%s\",\n" +
                           " \"emotion\":{\"label\":\"%s\",\"intensity\":%s},\n" +
                           " \"translation\":\"%s\",\n" +
                           " \"advice\":[\n%s\n" +
                           " ],\n" +
                           " \"confidence\":%s,\"risk_flags\":%s\n" +
                           "}",
                           id, escapeJson(inputText),
                           id, escapeJson(surfaceMeaning), escapeJson(hiddenMeaning),
                           escapeJson(emotionLabel), emotionIntensity, escapeJson(translation),
                           adviceJson.toString(), confidence, riskFlagsJson);
    }
    
    /**
     * JSON 문자열에서 특수문자 이스케이프
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
