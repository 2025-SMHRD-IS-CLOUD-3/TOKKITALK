package com.tokkitalk.db;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlManager {

    // 사용하는 DB의 기본 정보(driver, url, username, pw)를 
    // 가지고 여러 개의 sqlSession을 생성할 수 있는 
    // sqlSessionFactory를 생성하고 관리하는 클래스
    // sqlSession : DB에 접근할 수 있는 권한, 입장권을 의미
    public static SqlSessionFactory sqlSessionFactory;

    // 클래스 로딩 시에 딱 한 번만 실행하고 더 이상 실행되지 않도록 설정
    static {
        // DB의 정보가 담긴 XML 파일 주소를 변수에 담음
        // 실제 환경에 맞게 경로를 설정해주세요
        String resource = "com/tokkitalk/db/mybatis-config.xml";  // mybatis-config.xml로 변경
        
        try {
            // 리소스를 읽어와서 SqlSessionFactory를 생성
            Reader reader = Resources.getResourceAsReader(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        } catch (IOException e) {
            // 예외가 발생한 경우 스택 트레이스를 출력하고, 해당 정보를 로깅할 수 있음
            e.printStackTrace();
            // 예외 처리 방식에 따라 적절한 로직을 작성해 주세요 (예: 종료, 오류 메시지 출력 등)
        }
    }

    // 다른 클래스에서 SqlSessionFactory를 가져오고 싶을 때 사용할 메서드
    // 리턴값으로는 static 블록에서 생성된 sqlSessionFactory 반환
    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}