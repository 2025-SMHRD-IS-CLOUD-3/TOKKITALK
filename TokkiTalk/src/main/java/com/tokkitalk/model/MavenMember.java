package com.tokkitalk.model;

public class MavenMember {

    private String userId;
    private String password;
    private String userName;
    private String gender;
    private String birthDate;

    // 기본 생성자
    public MavenMember() {}

    // 생성자
    public MavenMember(String userId, String password, String userName, String gender, String birthDate) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    // 로그인 시 사용하는 생성자
    public MavenMember(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // getter, setter 메서드들
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}