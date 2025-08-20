package com.tokkitalk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlManager {
    
    // 정적 메서드로 DB 연결을 제공합니다.
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Oracle JDBC 드라이버 로드
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            // 데이터베이스 연결 정보 (mybatis-db.xml 참고)
            String url = "jdbc:oracle:thin:@project-db-campus.smhrd.com:1524:xe";
            String user = "campus_24IS_CLOUD3_p2_1";
            String password = "smhrd1";
            
            conn = DriverManager.getConnection(url, user, password);
            
        } catch (Exception e) {
            System.err.println("DB 연결 오류!");
            e.printStackTrace();
        }
        return conn;
    }
    
    // 자원을 안전하게 닫는 오버로드된 정적 메서드들을 제공합니다.
    public static void close(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(PreparedStatement psmt, Connection conn) {
        try {
            if (psmt != null) psmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(ResultSet rs, PreparedStatement psmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (psmt != null) psmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}