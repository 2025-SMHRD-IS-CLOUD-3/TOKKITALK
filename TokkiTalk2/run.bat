@echo off
echo ========================================
echo TokkiTalk2 실행 스크립트
echo ========================================
echo.

echo 1. Maven 빌드 중...
call mvn clean package
if %errorlevel% neq 0 (
    echo 빌드 실패!
    pause
    exit /b 1
)

echo.
echo 2. Tomcat 서버 시작 중... (포트: 8081)
echo 브라우저에서 http://localhost:8081/TokkiTalk2/main.jsp 접속
echo.
call mvn tomcat7:run

pause
