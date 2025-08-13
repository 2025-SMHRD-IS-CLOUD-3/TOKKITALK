package com.tokkitalk.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberDAO {

    // 회원가입 메서드
    public boolean insertMember(MavenMember member) {
        String sql = "INSERT INTO TB_USER_INFO (USER_ID, USER_PW, USER_NAME, GENDER, USER_DATE) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getUserId());
            pstmt.setString(2, member.getPassword());
            pstmt.setString(3, member.getUserName());
            pstmt.setString(4, member.getGender());
            pstmt.setString(5, member.getBirthDate());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;  // 성공적으로 추가된 행이 있다면 true 반환

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 로그인 메서드
    public boolean validateUser(String userId, String password) {
        String sql = "SELECT USER_ID, USER_PW FROM TB_USER_INFO WHERE USER_ID = ? AND USER_PW = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // 데이터가 있으면 true

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
