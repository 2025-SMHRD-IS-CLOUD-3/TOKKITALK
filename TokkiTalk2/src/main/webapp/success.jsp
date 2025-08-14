<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
    // 로그인되지 않은 사용자는 main.jsp로 리다이렉트
    if (session.getAttribute("member") == null) {
        response.sendRedirect("main.jsp?msg=login_required");
        return;
    }
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>TokkiTalk - 로그인 성공</title>
<style>
    body {
        font-family: 'Arial', sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        margin: 0;
        padding: 0;
        min-height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    
    .container {
        background: white;
        border-radius: 20px;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        padding: 40px;
        text-align: center;
        max-width: 500px;
        width: 90%;
    }
    
    .success-icon {
        width: 80px;
        height: 80px;
        background: #4CAF50;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
        color: white;
        font-size: 40px;
    }
    
    h1 {
        color: #333;
        margin-bottom: 10px;
        font-size: 28px;
    }
    
    .welcome-text {
        color: #666;
        margin-bottom: 30px;
        font-size: 16px;
    }
    
    .user-info {
        background: #f8f9fa;
        border-radius: 10px;
        padding: 20px;
        margin: 20px 0;
        text-align: left;
    }
    
    .user-info h3 {
        color: #333;
        margin-bottom: 15px;
        font-size: 18px;
    }
    
    .info-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 8px 0;
        border-bottom: 1px solid #eee;
    }
    
    .info-label {
        font-weight: bold;
        color: #555;
    }
    
    .info-value {
        color: #333;
    }
    
    .btn {
        display: inline-block;
        padding: 12px 30px;
        margin: 10px;
        border: none;
        border-radius: 25px;
        text-decoration: none;
        font-weight: bold;
        transition: all 0.3s ease;
        cursor: pointer;
    }
    
    .btn-primary {
        background: #667eea;
        color: white;
    }
    
    .btn-primary:hover {
        background: #5a6fd8;
        transform: translateY(-2px);
    }
    
    .btn-secondary {
        background: #6c757d;
        color: white;
    }
    
    .btn-secondary:hover {
        background: #5a6268;
        transform: translateY(-2px);
    }
    
    .logout-btn {
        background: #dc3545;
        color: white;
    }
    
    .logout-btn:hover {
        background: #c82333;
        transform: translateY(-2px);
    }
</style>
</head>
<body>
    <div class="container">
        <div class="success-icon">✓</div>
        <h1>로그인 성공!</h1>
        <p class="welcome-text">TokkiTalk에 오신 것을 환영합니다!</p>
        
        <c:if test="${not empty sessionScope.member}">
            <div class="user-info">
                <h3>회원 정보</h3>
                <div class="info-row">
                    <span class="info-label">아이디:</span>
                    <span class="info-value">${sessionScope.member.user_id}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">이름:</span>
                    <span class="info-value">${sessionScope.member.user_name}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">성별:</span>
                    <span class="info-value">${sessionScope.member.gender}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">가입일:</span>
                    <span class="info-value"><fmt:formatDate value="${sessionScope.member.user_date}" pattern="yyyy-MM-dd"/></span>
                </div>
            </div>
        </c:if>
        
        <div>
            <a href="main.jsp" class="btn btn-primary">메인으로 돌아가기</a>
            <a href="logout" class="btn logout-btn">로그아웃</a>
        </div>
    </div>
</body>
</html>
