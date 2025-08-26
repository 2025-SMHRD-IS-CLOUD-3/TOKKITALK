package com.tokkitalk.model;

public class User {
    private String userId;

    // 생성자
    public User() {}

    public User(String userId) {
        this.userId = userId;
    }

    // Getter와 Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}