# Maven 설치 가이드

## Windows에서 Maven 설치하기

### 1. Maven 다운로드
1. https://maven.apache.org/download.cgi 접속
2. **apache-maven-3.9.6-bin.zip** 다운로드

### 2. 압축 해제
1. 다운로드한 zip 파일을 `C:\Program Files\Apache\maven` 폴더에 압축 해제
2. 폴더 구조: `C:\Program Files\Apache\maven\apache-maven-3.9.6`

### 3. 환경 변수 설정
1. **시스템 환경 변수** 열기
   - Windows 키 + R → `sysdm.cpl` 입력 → 고급 탭 → 환경 변수
2. **시스템 변수**에 새로 만들기:
   - 변수 이름: `MAVEN_HOME`
   - 변수 값: `C:\Program Files\Apache\maven\apache-maven-3.9.6`
3. **Path** 변수 편집:
   - 새로 만들기: `%MAVEN_HOME%\bin`

### 4. 설치 확인
터미널에서 다음 명령어 실행:
```bash
mvn -version
```

## 또는 간단한 방법: Eclipse 내장 Maven 사용

### 1. Eclipse에서 Maven 프로젝트로 변환
1. 프로젝트 우클릭 → **Configure** → **Convert to Maven Project**
2. Group Id: `com.tokkitalk`
3. Artifact Id: `TokkiTalk2`
4. Version: `1.0-SNAPSHOT`
5. **Finish**

### 2. Eclipse에서 실행
1. 프로젝트 우클릭 → **Run As** → **Maven build...**
2. Goals: `tomcat7:run`
3. **Run**

## 또는 Tomcat 직접 실행

### 1. Tomcat 다운로드
1. https://tomcat.apache.org/download-90.cgi 접속
2. **apache-tomcat-9.0.107-windows-x64.zip** 다운로드

### 2. 압축 해제
1. `C:\Program Files\Apache\Tomcat9` 폴더에 압축 해제

### 3. 프로젝트 배포
1. `src/main/webapp` 폴더의 모든 내용을 `C:\Program Files\Apache\Tomcat9\webapps\TokkiTalk2` 폴더에 복사
2. Java 클래스 파일들을 `C:\Program Files\Apache\Tomcat9\webapps\TokkiTalk2\WEB-INF\classes` 폴더에 복사

### 4. Tomcat 실행
```bash
cd "C:\Program Files\Apache\Tomcat9\bin"
startup.bat
```

### 5. 브라우저 접속
```
http://localhost:8080/TokkiTalk2/
```
