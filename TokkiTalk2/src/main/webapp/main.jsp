<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
    // 이미 로그인된 사용자가 있으면 success.jsp로 리다이렉트
    if (session.getAttribute("member") != null) {
        response.sendRedirect("success.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<title>TokkiTalk - 메인</title>
<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }
    
    body {
        font-family: 'Arial', sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
    }
    
    .container {
        background: white;
        border-radius: 20px;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        max-width: 900px;
        width: 100%;
        display: flex;
        min-height: 600px;
    }
    
    .left-panel {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 40px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        flex: 1;
    }
    
    .logo {
        font-size: 2.5em;
        font-weight: bold;
        margin-bottom: 20px;
    }
    
    .tagline {
        font-size: 1.2em;
        opacity: 0.9;
        margin-bottom: 30px;
    }
    
    .features {
        list-style: none;
        text-align: left;
    }
    
    .features li {
        margin: 10px 0;
        padding-left: 20px;
        position: relative;
    }
    
    .features li:before {
        content: "✓";
        position: absolute;
        left: 0;
        color: #4CAF50;
        font-weight: bold;
    }
    
    .right-panel {
        flex: 1;
        padding: 40px;
        display: flex;
        flex-direction: column;
        justify-content: center;
    }
    
    .form-container {
        width: 100%;
    }
    
    .form-toggle {
        display: flex;
        margin-bottom: 30px;
        background: #f8f9fa;
        border-radius: 25px;
        padding: 5px;
    }
    
    .toggle-btn {
        flex: 1;
        padding: 12px;
        border: none;
        background: transparent;
        border-radius: 20px;
        cursor: pointer;
        font-weight: bold;
        transition: all 0.3s ease;
    }
    
    .toggle-btn.active {
        background: #667eea;
        color: white;
    }
    
    .form {
        display: none;
    }
    
    .form.active {
        display: block;
    }
    
    .form-group {
        margin-bottom: 20px;
    }
    
    .form-group label {
        display: block;
        margin-bottom: 8px;
        font-weight: bold;
        color: #333;
    }
    
    .form-group input {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e9ecef;
        border-radius: 10px;
        font-size: 16px;
        transition: border-color 0.3s ease;
    }
    
    .form-group input:focus {
        outline: none;
        border-color: #667eea;
    }
    
    .form-group select {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e9ecef;
        border-radius: 10px;
        font-size: 16px;
        transition: border-color 0.3s ease;
        background: white;
    }
    
    .form-group select:focus {
        outline: none;
        border-color: #667eea;
    }
    
    .submit-btn {
        width: 100%;
        padding: 15px;
        background: #667eea;
        color: white;
        border: none;
        border-radius: 10px;
        font-size: 16px;
        font-weight: bold;
        cursor: pointer;
        transition: all 0.3s ease;
        margin-top: 10px;
    }
    
    .submit-btn:hover {
        background: #5a6fd8;
        transform: translateY(-2px);
    }
    
    .alert {
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 20px;
        font-weight: bold;
    }
    
    .alert-success {
        background: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }
    
    .alert-danger {
        background: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }
    
    .alert-info {
        background: #d1ecf1;
        color: #0c5460;
        border: 1px solid #bee5eb;
    }
    
    .alert-warning {
        background: #fff3cd;
        color: #856404;
        border: 1px solid #ffeaa7;
    }
    
    @media (max-width: 768px) {
        .container {
            flex-direction: column;
            max-width: 400px;
        }
        
        .left-panel {
            padding: 30px 20px;
        }
        
        .right-panel {
            padding: 30px 20px;
        }
    }
</style>
</head>
<body>
    <div class="container">
        <div class="left-panel">
            <div class="logo">TokkiTalk</div>
            <div class="tagline">친구들과 함께하는 즐거운 대화</div>
            <ul class="features">
                <li>실시간 채팅</li>
                <li>그룹 대화</li>
                <li>파일 공유</li>
                <li>이모티콘 지원</li>
            </ul>
        </div>
        
        <div class="right-panel">
            <div class="form-container">
                <div class="form-toggle">
                    <button class="toggle-btn active" onclick="showForm('login')">로그인</button>
                    <button class="toggle-btn" onclick="showForm('join')">회원가입</button>
                </div>
                
                <!-- 메시지 알림 -->
                <% String msg = request.getParameter("msg"); %>
                <% if (msg != null) { %>
                    <% if (msg.equals("join_success")) { %>
                        <div class="alert alert-success">회원가입이 성공적으로 완료되었습니다!</div>
                    <% } else if (msg.equals("join_fail")) { %>
                        <div class="alert alert-danger">회원가입에 실패했습니다. 다시 시도해주세요.</div>
                    <% } else if (msg.equals("login_fail")) { %>
                        <div class="alert alert-danger">아이디 또는 비밀번호가 올바르지 않습니다.</div>
                    <% } else if (msg.equals("logout_success")) { %>
                        <div class="alert alert-info">로그아웃되었습니다.</div>
                    <% } else if (msg.equals("login_required")) { %>
                        <div class="alert alert-warning">로그인이 필요합니다.</div>
                    <% } %>
                <% } %>
                
                <!-- 로그인 폼 -->
                <form class="form active" id="loginForm" action="login" method="post">
                    <div class="form-group">
                        <label for="loginId">아이디</label>
                        <input type="text" id="loginId" name="id" required>
                    </div>
                    <div class="form-group">
                        <label for="loginPw">비밀번호</label>
                        <input type="password" id="loginPw" name="pw" required>
                    </div>
                    <button type="submit" class="submit-btn">로그인</button>
                </form>
                
                <!-- 회원가입 폼 -->
                <form class="form" id="joinForm" action="join" method="post">
                    <div class="form-group">
                        <label for="joinId">아이디</label>
                        <input type="text" id="joinId" name="id" required>
                    </div>
                    <div class="form-group">
                        <label for="joinPw">비밀번호</label>
                        <input type="password" id="joinPw" name="pw" required>
                    </div>
                    <div class="form-group">
                        <label for="name">이름</label>
                        <input type="text" id="name" name="name" required>
                    </div>
                    <div class="form-group">
                        <label for="gender">성별</label>
                        <select id="gender" name="gender" required>
                            <option value="">선택하세요</option>
                            <option value="남성">남성</option>
                            <option value="여성">여성</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="user_date">가입일</label>
                        <input type="date" id="user_date" name="user_date" required>
                    </div>
                    <button type="submit" class="submit-btn">회원가입</button>
                </form>
            </div>
        </div>
    </div>
    
    <script>
        function showForm(formType) {
            // 모든 폼 숨기기
            document.querySelectorAll('.form').forEach(form => {
                form.classList.remove('active');
            });
            
            // 모든 버튼 비활성화
            document.querySelectorAll('.toggle-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            
            // 선택된 폼과 버튼 활성화
            if (formType === 'login') {
                document.getElementById('loginForm').classList.add('active');
                document.querySelector('.toggle-btn:first-child').classList.add('active');
            } else {
                document.getElementById('joinForm').classList.add('active');
                document.querySelector('.toggle-btn:last-child').classList.add('active');
            }
        }
        
        // 페이지 로드 시 URL 파라미터에 따라 폼 선택
        window.onload = function() {
            const urlParams = new URLSearchParams(window.location.search);
            const msg = urlParams.get('msg');
            
            if (msg === 'join_success' || msg === 'join_fail') {
                showForm('login');
            }
        };
    </script>
</body>
</html>