package com.tokkitalk.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.session.SqlSession;
import com.tokkitalk.db.SqlManager;

public class MemberDAO {

    // 회원가입 메서드
    public boolean insertMember(MavenMember member) {
        try (SqlSession sqlSession = SqlManager.getSqlSessionFactory().openSession(true)) {
            // MyBatis를 통해 "insertMember"라는 SQL을 실행
            int rowsAffected = sqlSession.insert("com.tokkitalk.model.MemberDAO.insertMember", member);
            return rowsAffected > 0; // 성공적으로 추가된 행이 있다면 true 반환
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 로그인 메서드
    public MavenMember login(MavenMember member) {
        try (SqlSession sqlSession = SqlManager.getSqlSessionFactory().openSession(true)) {
            return sqlSession.selectOne("com.tokkitalk.model.MemberDAO.validateUser", member);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
