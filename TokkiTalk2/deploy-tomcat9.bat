@echo off
setlocal enabledelayedexpansion

echo ========================================
echo TokkiTalk2 Tomcat 9 배포 스크립트
echo ========================================
echo.

REM TOMCAT_HOME이 설정되어 있지 않으면 기본 경로 시도
if "%TOMCAT_HOME%"=="" (
    set TOMCAT_HOME=C:\Program Files\Apache\Tomcat9
)

if not exist "%TOMCAT_HOME%\bin\startup.bat" (
    echo [오류] TOMCAT_HOME 경로가 올바르지 않습니다.
    echo 현재 설정: %TOMCAT_HOME%
    echo 환경변수 TOMCAT_HOME 을 Tomcat 9 설치 경로로 설정하세요.
    pause
    exit /b 1
)

echo 1) Maven 빌드 중...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 빌드 실패!
    pause
    exit /b 1
)

echo.
echo 2) Tomcat 9 배포 중...
if not exist target\TokkiTalk2.war (
    echo [오류] target\TokkiTalk2.war 파일이 없습니다.
    pause
    exit /b 1
)

REM 기존 배포 삭제
if exist "%TOMCAT_HOME%\webapps\TokkiTalk2" (
    rmdir /s /q "%TOMCAT_HOME%\webapps\TokkiTalk2"
)
if exist "%TOMCAT_HOME%\webapps\TokkiTalk2.war" (
    del /q "%TOMCAT_HOME%\webapps\TokkiTalk2.war"
)

copy /y target\TokkiTalk2.war "%TOMCAT_HOME%\webapps\TokkiTalk2.war" >nul
echo 배포 파일 복사 완료

echo.
echo 3) Tomcat 재시작...
call "%TOMCAT_HOME%\bin\shutdown.bat" >nul 2>&1
timeout /t 2 >nul
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo 배포 완료. 브라우저에서 다음 주소로 접속하세요:
echo http://localhost:8080/TokkiTalk2/main.html
echo (포트가 다르면 실제 Tomcat 포트에 맞게 변경)

pause


