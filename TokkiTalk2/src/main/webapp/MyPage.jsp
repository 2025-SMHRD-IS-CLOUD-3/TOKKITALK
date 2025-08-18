<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% boolean isLoggedIn = (session.getAttribute("member") != null); %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TOKKI TALK - 마이페이지</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans KR', sans-serif;
            background: #f8f9fa;
            color: #333;
        }
        
        /* 헤더 */
        .header {
            background: white;
            padding: 20px 0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        
        .nav-container {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px;
        }
        
        .logo {
            font-size: 28px;
            font-weight: bold;
            color: #FFD200;
            letter-spacing: 2px;
        }
        .logo a {
            color: #FFD200; /* Yellow color */
            text-decoration: none;
        }
        
        .nav-menu {
            display: flex;
            gap: 40px;
        }
        
        .nav-item {
            color: #666;
            text-decoration: none;
            font-size: 16px;
            font-weight: 500;
            transition: color 0.3s;
        }
        
        .nav-item:hover {
            color: #ff6b6b;
        }
        
        .nav-buttons a {
            display: flex;
            gap: 15px;
            text-decoration: none;
            color: black;
        }
        .nav-buttons:hover {
        	color: #ff6b6b;
        }
        
        .btn-login, .btn-logout {
            background: #f1f3f4;
            color: #666;
            border: none;
            padding: 12px 24px;
            border-radius: 25px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.3s;
        }
        
        .btn-login:hover, .btn-logout:hover {
            background: #ff6b6b;
        }
        
        .btn-signup, .btn-edit-profile {
            background: #FFD200;
            color: black;
            border: none;
            padding: 12px 24px;
            border-radius: 25px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.3s;
        }
        
        .btn-signup:hover, .btn-edit-profile:hover {
            background: #ff5252;
            color: white;
        }
        
        /* 마이페이지 메인 콘텐츠 */
        .mypage-container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 20px;
        }
        
        .mypage-heading {
            font-size: 36px;
            font-weight: bold;
            margin-bottom: 40px;
            text-align: center;
            color: #333;
        }
        
        .profile-section {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.05);
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-bottom: 40px;
        }

        .profile-info {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .profile-info h2 {
            font-size: 28px;
            font-weight: bold;
            margin-bottom: 5px;
        }

        .profile-info p {
            font-size: 16px;
            color: #666;
            margin-bottom: 15px;
        }

        .mypage-content {
            display: flex;
            flex-direction: column;
            gap: 40px;
        }

        .content-card {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.05);
        }

        .content-card h3 {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 20px;
            border-bottom: 2px solid #eee;
            padding-bottom: 10px;
        }
        
        .content-card ul {
            list-style: none;
            padding: 0;
        }

        .content-card li {
            padding: 15px 0;
            border-bottom: 1px solid #f0f0f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .content-card li:last-child {
            border-bottom: none;
        }

        .list-item-link {
            text-decoration: none;
            color: inherit;
            display: flex;
            justify-content: space-between;
            align-items: center;
            width: 100%;
            cursor: pointer;
        }

        .list-item-link:hover {
            color: #ff6b6b;
        }

        .list-item-title {
            font-size: 16px;
            font-weight: 500;
        }

        .list-item-date {
            font-size: 14px;
            color: #999;
        }
        
        /* 푸터 */
        .footer {
            background: white;
            padding: 40px 0;
            margin-top: 80px;
            border-top: 1px solid #eee;
        }
        
        .footer-container {
            max-width: 1200px;
            margin: 0 auto;
            text-align: center;
            padding: 0 20px;
        }
        
        .footer-text {
            color: #999;
            font-size: 14px;
        }
        
        /* 모달 스타일 */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0,0,0,0.4);
            justify-content: center;
            align-items: center;
        }

        .modal-content {
            background-color: #fefefe;
            margin: auto;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            width: 90%;
            max-width: 600px;
            position: relative;
        }

        .close-btn {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
            position: absolute;
            top: 10px;
            right: 20px;
            cursor: pointer;
        }

        .close-btn:hover,
        .close-btn:focus {
            color: #333;
            text-decoration: none;
            cursor: pointer;
        }
        
        .chat-title {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }

        .chat-messages {
            display: flex;
            flex-direction: column;
            gap: 15px;
            max-height: 400px;
            overflow-y: auto;
        }

        .message-row {
            display: flex;
            align-items: flex-start;
        }

        .message-row.sent {
            justify-content: flex-end;
        }
        
        .message-row.received {
            justify-content: flex-start;
        }

        .message-bubble {
            max-width: 70%;
            padding: 12px 18px;
            border-radius: 20px;
            word-wrap: break-word;
        }

        .message-row.sent .message-bubble {
            background-color: #FFD200;
            color: #333;
            border-top-right-radius: 5px;
        }
        
        .message-row.received .message-bubble {
            background-color: #f1f3f4;
            color: #333;
            border-top-left-radius: 5px;
        }

        .message-timestamp {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }
        
        .message-row.sent .message-timestamp {
            margin-right: 10px;
            text-align: right;
        }
        
        .message-row.received .message-timestamp {
            margin-left: 10px;
        }
        
        .loading-message {
            text-align: center;
            font-style: italic;
            color: #999;
        }
        
        /* 프로필 수정 모달 스타일 */
        #profileModal .modal-content {
            max-width: 450px;
        }
        .profile-form h2 {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 20px;
        }
        .profile-form label {
            display: block;
            font-size: 14px;
            color: #555;
            margin-bottom: 5px;
            font-weight: 500;
        }
        .profile-form input {
            width: 100%;
            padding: 12px;
            margin-bottom: 20px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
        }
        .profile-form input:focus {
            outline: none;
            border-color: #FFD200;
        }
        .profile-form .btn-save {
            width: 100%;
            padding: 15px;
            background: #FFD200;
            color: #333;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.3s;
        }
        .profile-form .btn-save:hover {
            background: #ff5252;
            color: white;
        }
        .error-message {
            color: #ff5252;
            font-size: 12px;
            margin-top: -15px;
            margin-bottom: 15px;
            display: none;
        }

        /* 반응형 */
        @media (max-width: 768px) {
            .nav-menu {
                display: none;
            }
            .mypage-heading {
                font-size: 28px;
            }
            .profile-section {
                padding: 20px;
            }
            .profile-info h2 {
                font-size: 24px;
            }
            .content-card {
                padding: 20px;
            }
            .content-card h3 {
                font-size: 20px;
            }
            .modal-content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="nav-container">
            <div class="logo"><a href="main.jsp">TOKKI TALK</a></div>
            <nav class="nav-menu">
                <a href="#" class="nav-item">소개</a>
                <a href=".SenseTest.html" class="nav-item">센스고사</a>
                <a href="MyPage.jsp" class="nav-item">마이페이지</a>
            </nav>
            <div class="nav-buttons">
                <button class="btn-logout"><a href="main.jsp">로그아웃</a></button>
                <% if (isLoggedIn) { %>
                    <form action="logout" method="post">
                        <button type="submit" class="btn-logout"><a href="Logout">로그아웃</a></button>
                    </form>
                <% } %>
            </div>
        </div>
    </header>

    <main class="mypage-container">
        <h1 class="mypage-heading">마이페이지</h1>
        
        <div class="profile-section">
            <div class="profile-info">
                <h2 id="userName">김토끼님</h2>
                <p id="userEmail">tokki_kim@email.com</p>
                <button class="btn-edit-profile" id="openProfileModal">프로필 수정</button>
            </div>
        </div>
        
        <div class="mypage-content">
            <div class="content-card">
                <h3>히스토리</h3>
                <ul id="chatList">
                    <li class="loading-message">대화 기록을 불러오는 중...</li>
                </ul>
            </div>
        </div>
    </main>
    
    <div id="chatModal" class="modal">
        <div class="modal-content">
            <span class="close-btn chat-close-btn">&times;</span>
            <div id="modalContent">
                </div>
        </div>
    </div>

    <div id="profileModal" class="modal">
        <div class="modal-content">
            <span class="close-btn profile-close-btn">&times;</span>
            <div class="profile-form">
                <h2>프로필 수정</h2>
                <label for="editName">이름</label>
                <input type="text" id="editName" placeholder="새 이름을 입력하세요">
                <label for="currentPassword">현재 비밀번호</label>
                <input type="password" id="currentPassword" placeholder="현재 비밀번호를 입력하세요">
                <label for="editPassword">새 비밀번호</label>
                <input type="password" id="editPassword" placeholder="새 비밀번호를 입력하세요">
                <label for="confirmPassword">비밀번호 재확인</label>
                <input type="password" id="confirmPassword" placeholder="비밀번호를 다시 입력하세요">
                <button class="btn-save" id="saveProfileBtn">저장</button>
            </div>
        </div>
    </div>

    <footer class="footer">
        <div class="footer-container">
            <p class="footer-text">© 2025 TOKKI TALK. All Rights Reserved.</p>
        </div>
    </footer>
    
    <script src="MyPage.js"></script>
</body>
</html>