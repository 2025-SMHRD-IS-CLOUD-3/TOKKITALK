<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TOKKI TALK - 센스고사</title>
    <style>
        /* CSS 코드는 그대로 유지 */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans KR', sans-serif;
            background: #e9e9e9;
            color: #333;
        }

        /* Header */
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

        .nav-buttons {
            display: flex;
            gap: 15px;
        }

        .btn-login {
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

        .btn-login:hover {
            background: #e8eaed;
        }

        .btn-signup {
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

        .btn-signup:hover {
            background: #ff5252;
        }
        
        /* Quiz Page specific styling */
        .quiz-container {
            display: flex;
            background: #FFD200;
            border-radius: 15px;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
            height: auto;
            max-width: 800px;
            margin: 0 auto;
        }

        .question-card {
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
            width: 100%;
            margin-bottom: 30px;
        }

        .question-title {
            font-size: 24px;
            font-weight: bold;
            color: #333;
            margin-bottom: 20px;
        }

        .chat-image {
            width: 100%;
            max-width: 400px;
            height: auto;
            border-radius: 10px;
            margin-bottom: 20px;
        }

        .answer-options {
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 15px;
        }

        .answer-option {
            background: white;
            border: 1px solid #ddd;
            padding: 20px;
            border-radius: 15px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
            text-align: left;
        }

        .answer-option:hover {
            background: #fffbe6;
            border-color: #FFD200;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
        }

        /* Footer */
        .footer {
            background: white;
            padding: 40px 0;
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

        /* Responsive design */
        @media (max-width: 768px) {
            .nav-menu {
                display: none;
            }

            .main-title {
                font-size: 36px;
            }
            .question-title {
                font-size: 20px;
            }
        }
        /* ... (기존 스타일 코드) ... */

        /* 결과 페이지 스타일링 */
        .result-card {
            background: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
            width: 100%;
            max-width: 500px;
        }

        .result-title {
            font-size: 32px;
            font-weight: bold;
            color: #FFD200;
            margin-bottom: 15px;
        }

        .result-text {
            font-size: 18px;
            line-height: 1.6;
            color: #555;
            margin-bottom: 30px;
        }

        .btn-share {
            background: #ff6b6b;
            color: white;
            border: none;
            padding: 15px 30px;
            border-radius: 25px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.3s;
        }

        .btn-share:hover {
            background: #e84a5f;
        }

        /* ... (기존 스타일 코드) ... */
    </style>
</head>
<body>
    <header class="header">
        <div class="nav-container">
            <div class="logo"><a href="main.jsp">TOKKI TALK</a></div>
            <nav class="nav-menu">
                <a href="#" class="nav-item">소개</a>
                <a href="SenseTest.jsp" class="nav-item">센스고사</a>
                <a href="MyPage.jsp" class="nav-item">마이페이지</a>
            </nav>
            <div class="nav-buttons">
                <button class="btn-login">로그인</button>
                <button class="btn-signup">회원가입</button>
            </div>
        </div>
    </header>

    <main class="quiz-container">
        <div class="question-card">
            <h2 class="question-title">Q1. 다음 카톡 이후 적절한 행동으로 <span style="color: #ff6b6b;">올바른</span> 것은?</h2>
            <img src="https://i.imgur.com/your_chat_image.png" alt="대화 내용" class="chat-image">
            <div class="answer-options">
                <div class="answer-option">A. 선택지 1 내용입니다.</div>
                <div class="answer-option">B. 선택지 2 내용입니다.</div>
                <div class="answer-option">C. 선택지 3 내용입니다.</div>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-container">
            <p class="footer-text">© 2025 TOKKI TALK. All Rights Reserved.</p>
        </div>
    </footer>
    
    <script src="SenseTest.js"></script>
</body>
</html>