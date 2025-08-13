package com.tokkitalk.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    // 로그인 메서드
    public boolean loginUser(String userId, String password) {
        String sql = "SELECT USER_ID, USER_PW FROM TB_USER_INFO WHERE USER_ID = ? AND USER_PW = ?";
        
        try (Connection conn = DatabaseUtil.getConnection(); // DB 연결
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 로그인 성공 시
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false; // 로그인 실패 시
    }
}