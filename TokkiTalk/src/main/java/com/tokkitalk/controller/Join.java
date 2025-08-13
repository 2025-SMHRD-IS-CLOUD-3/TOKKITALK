package com.tokkitalk.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Join {

    private MemberDAO memberDAO;

    public Join() {
        memberDAO = new MemberDAO();
    }

    // 회원가입 메서드
    public boolean registerUser(MavenMember member) {
        return memberDAO.insertMember(member);
    }
}
