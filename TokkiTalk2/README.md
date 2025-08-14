# TokkiTalk2

## 프로젝트 개요
로그인, 회원가입, 로그인 성공 기능을 구현한 Java Web Application입니다.

## 기술 스택
- **Backend**: Java Servlet, JSP
- **Database**: Oracle Database
- **ORM**: MyBatis
- **Build Tool**: Maven
- **Server**: Apache Tomcat v9.0
- **IDE**: Eclipse, Cursor

## 주요 기능
- 사용자 회원가입
- 사용자 로그인/로그아웃
- 세션 관리
- 데이터베이스 연동

## 프로젝트 구조
```
TokkiTalk2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tokkitalk/
│   │   │       ├── controller/     # 서블릿 컨트롤러
│   │   │       ├── model/          # 모델 클래스
│   │   │       └── db/             # 데이터베이스 관련
│   │   ├── resources/              # MyBatis 설정 파일
│   │   └── webapp/                 # JSP, HTML, CSS
│   └── test/
├── target/
├── pom.xml                         # Maven 설정
├── run.bat                         # Windows 실행 스크립트
├── run.sh                          # Linux/Mac 실행 스크립트
└── README.md
```

## 설치 및 실행

### 1. Eclipse에서 실행
1. 프로젝트를 Eclipse에 Import
2. Tomcat v9.0 서버 설정 (포트: 8081)
3. 프로젝트를 서버에 배포
4. 서버 시작
5. 브라우저에서 접속

### 2. Cursor에서 실행

#### 방법 1: 실행 스크립트 사용
**Windows:**
```bash
run.bat
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

#### 방법 2: Maven 명령어 직접 실행
```bash
# Maven 빌드
mvn clean package

# Tomcat 서버 시작 (포트 8081)
mvn tomcat7:run
```

### 3. 브라우저 접속
- **메인 페이지**: http://localhost:8081/TokkiTalk2/main.jsp
- **테스트 페이지**: http://localhost:8081/TokkiTalk2/test
- **로그인 성공 페이지**: http://localhost:8081/TokkiTalk2/success.jsp

## 데이터베이스 설정
- **서버**: project-db-campus.smhrd.com:1524:xe
- **계정**: campus_24IS_CLOUD3_p2_1
- **테이블**: TB_USER_INFO

## 테스트 계정
- **ID**: test
- **PW**: 1234

## 사용법
1. 메인 페이지에서 회원가입 또는 로그인
2. 로그인 성공 시 success.jsp로 이동
3. 로그아웃 시 main.jsp로 리다이렉트

## 문제 해결
- **포트 충돌**: 8081 포트가 사용 중인 경우 다른 포트 사용
- **데이터베이스 연결 실패**: mybatis-db.xml의 연결 정보 확인
- **로그인 실패**: /test 페이지에서 테스트 데이터 삽입 후 재시도
- **빌드 실패**: Maven과 JDK 버전 확인 (JDK 1.8 이상 필요)

## 개발 환경
- **JDK**: 1.8 이상
- **Maven**: 3.6 이상
- **Tomcat**: 9.0
- **Oracle**: 21c 이상
