package com.tokkitalk.analysis.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public String userId;
    public String role; // "user" or "model"
    public String message;
    public String imageUrl;
    public String imageBase64;
    public LocalDateTime timestamp;

    public ChatHistoryDTO(String userId, String role, String message, String imageUrl, String imageBase64, LocalDateTime timestamp) {
        this.userId = userId;
        this.role = role;
        this.message = message;
        this.imageUrl = imageUrl;
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
    }

    // 기본 생성자
    public ChatHistoryDTO() {}

    // Getter and Setter methods for all fields
    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
