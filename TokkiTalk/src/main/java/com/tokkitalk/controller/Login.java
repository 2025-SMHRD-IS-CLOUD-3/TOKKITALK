package com.tokkitalk.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private MemberDAO memberDAO;

    public Login() {
        memberDAO = new MemberDAO();
    }

    // 로그인 메서드
    public boolean loginUser(String userId, String password) {
        return memberDAO.validateUser(userId, password);
    }
}