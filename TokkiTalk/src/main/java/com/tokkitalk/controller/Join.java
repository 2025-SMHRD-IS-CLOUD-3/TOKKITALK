package com.tokkitalk.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Join {

    // 회원가입 메서드
    public boolean registerUser(String userId, String password, String userName, String gender, String birthDate) {
        String sql = "INSERT INTO TB_USER_INFO (USER_ID, USER_PW, USER_NAME, GENDER, USER_DATE) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();  // DB 연결
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            pstmt.setString(3, userName);
            pstmt.setString(4, gender);
            pstmt.setString(5, birthDate);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;  // 성공적으로 추가된 행이 있다면 true 반환

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
