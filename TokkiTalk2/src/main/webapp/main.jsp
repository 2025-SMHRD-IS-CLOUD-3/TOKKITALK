<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 세션에서 로그인 정보를 가져옵니다.
    boolean isLoggedIn = (session.getAttribute("member") != null);
    
    // URL 파라미터에서 메시지(msg)를 가져옵니다.
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TOKKI TALK</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans KR', sans-serif; background: #f8f9fa; color: #333; }
        .header { background: white; padding: 20px 0; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .nav-container { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
        .logo { font-size: 28px; font-weight: bold; color: #FFD200; letter-spacing: 2px; cursor: pointer; transition: color 0.3s; }
        .logo:hover { color: #ff6b6b; }
        .nav-menu { display: flex; gap: 40px; }
        .nav-item { color: #666; text-decoration: none; font-size: 16px; font-weight: 500; transition: color 0.3s; }
        .nav-item:hover { color: #ff6b6b; }
        .nav-buttons { display: flex; gap: 15px; }
        .btn-login { background: #f1f3f4; color: black; border: none; padding: 12px 24px; border-radius: 25px; font-size: 14px; font-weight: 500; cursor: pointer; transition: background 0.3s; }
        .btn-login:hover { background: #ff6b6b; color: white; }
        .btn-signup { background: #FFD200; color: black; border: none; padding: 12px 24px; border-radius: 25px; font-size: 14px; font-weight: 500; cursor: pointer; transition: background 0.3s; }
        .btn-signup:hover { background: #ff5252; }
        .btn-logout { background: #f1f3f4; color: black; border: none; padding: 12px 24px; border-radius: 25px; font-size: 14px; font-weight: 500; cursor: pointer; transition: background 0.3s; }
        .btn-logout:hover { background: #ff6b6b; color: white; }
        .alert { text-align: center; padding: 15px; margin-bottom: 20px; border-radius: 10px; font-weight: 600; display: none; }
        .alert-success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .alert-danger { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .alert-info { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
        .alert.show { display: block; }
        .main-container { max-width: 1200px; margin: 0 auto; padding: 80px 20px; min-height: 70vh; }
        .content-sections-wrapper { display: grid; grid-template-columns: 1fr 1fr; gap: 60px; align-items: start; }
        .content-section { max-width: 100%; padding: 40px; background: white; border-radius: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); transition: transform 0.3s ease, box-shadow 0.3s ease; }
        .content-section:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }
        .main-title { font-size: 36px; font-weight: bold; color: #333; line-height: 1.3; margin-bottom: 25px; }
        .main-description { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 35px; }
        .cta-button { background: #FFD200; color: black; border: none; padding: 15px 35px; border-radius: 30px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s; display: inline-flex; align-items: center; gap: 10px; width: 100%; justify-content: center; }
        .cta-button:hover { background: #ff5252; transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 107, 107, 0.3); }
        .icon { font-size: 20px; }
        .footer { background: white; padding: 40px 0; margin-top: 80px; border-top: 1px solid #eee; }
        .footer-container { max-width: 1200px; margin: 0 auto; text-align: center; padding: 0 20px; }
        .footer-text { color: #999; font-size: 14px; }
        .modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 1000; justify-content: center; align-items: center; }
        .modal-content { background: white; padding: 40px; border-radius: 20px; width: 400px; max-width: 90%; position: relative; text-align: center; }
        .modal-close { position: absolute; top: 15px; right: 20px; background: none; border: none; font-size: 24px; cursor: pointer; color: #333; }
        .modal-title { font-size: 24px; font-weight: bold; color: #333; margin-bottom: 30px; }
        .input-group { margin-bottom: 20px; }
        .modal-input { width: 100%; padding: 15px 20px; border: 2px solid #FFD200; border-radius: 8px; font-size: 16px; background: white; box-sizing: border-box; }
        .modal-input::placeholder { color: #999; }
        .character-icons { display: flex; justify-content: center; gap: 20px; margin: 30px 0; }
        .character-icon { width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; cursor: pointer; transition: transform 0.3s; }
        .character-icon:hover { transform: scale(1.1); }
        .icon-cool { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .icon-cute { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        .icon-smart { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        .forgot-password { color: #666; font-size: 14px; margin: 20px 0; cursor: pointer; }
        .forgot-password:hover { text-decoration: underline; }
        .divider { height: 1px; background: #333; margin: 20px 0; opacity: 0.3; }
        .modal-signup-btn { background: transparent; color: #333; border: 2px solid #333; padding: 15px 40px; border-radius: 25px; font-size: 16px; font-weight: 600; cursor: pointer; width: 100%; transition: all 0.3s; }
        .modal-signup-btn:hover { background: #333; color: white; }
        .modal-login-btn { background: #FFD200; color: black; border: none; padding: 15px 40px; border-radius: 25px; font-size: 16px; font-weight: 600; cursor: pointer; width: 100%; transition: all 0.3s; }
        .modal-login-btn:hover { background: #e6bd00; }
        .signup-modal-content { background: white; padding: 40px; border-radius: 20px; width: 500px; max-width: 90%; position: relative; text-align: left; }
        .signup-title { font-size: 24px; font-weight: bold; color: #333; margin-bottom: 10px; }
        .signup-underline { width: 60px; height: 3px; background: #FFD200; margin-bottom: 30px; }
        .signup-input-row { display: flex; gap: 10px; margin-bottom: 15px; }
        .signup-input { width: 100%; padding: 15px 20px; border: 2px solid #FFD200; border-radius: 8px; font-size: 14px; background: white; box-sizing: border-box; height: 50px; }
        .signup-input::placeholder { color: #999; }
        .duplicate-check-btn { background: #FFD200; color: black; border: none; padding: 15px 20px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; white-space: nowrap; height: 50px; }
        .duplicate-check-btn:hover { background: #e6bd00; }
        .date-input { width: 100%; padding: 15px 20px; border: 2px solid #FFD200; border-radius: 8px; font-size: 14px; background: white; box-sizing: border-box; height: 50px; }
        .gender-group { display: flex; align-items: center; gap: 20px; margin: 15px 0; }
        .gender-option { display: flex; align-items: center; gap: 8px; }
        .gender-radio { width: 20px; height: 20px; border: 2px solid #ddd; border-radius: 50%; cursor: pointer; position: relative; }
        .gender-radio.checked { border-color: #FFD200; background: #FFD200; }
        .gender-radio.checked::after { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 8px; height: 8px; background: white; border-radius: 50%; }
        .gender-label { font-size: 16px; cursor: pointer; }
        .signup-buttons { display: flex; gap: 15px; margin-top: 30px; }
        .signup-confirm-btn, .signup-cancel-btn { flex: 1; padding: 18px; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
        .signup-confirm-btn { background: #FFD200; color: black; }
        .signup-confirm-btn:hover { background: #e6bd00; }
        .signup-cancel-btn { background: #f1f3f4; color: #666; }
        .signup-cancel-btn:hover { background: #e8eaed; }
        @media (max-width: 768px) {
            .nav-menu { display: none; }
            .content-sections-wrapper { grid-template-columns: 1fr; gap: 40px; padding: 0 10px; }
            .content-section { padding: 30px 25px; }
            .main-title { font-size: 28px; margin-bottom: 20px; }
            .main-description { font-size: 15px; margin-bottom: 25px; }
            .cta-button { padding: 12px 25px; font-size: 15px; }
            .modal-content { width: 350px; padding: 30px; }
            .signup-modal-content { width: 400px; padding: 30px; }
        }
        @media (max-width: 1024px) and (min-width: 769px) {
            .content-sections-wrapper { gap: 40px; }
            .main-title { font-size: 32px; }
            .main-description { font-size: 15px; }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="nav-container">
            <div class="logo" onclick="goHome()">TOKKI TALK</div>
            <nav class="nav-menu">
                <a href="/권동환/K소개페이지.html" class="nav-item">소개</a>
                <a href="/권동환/K센스테스트.html" class="nav-item">센스고사</a>
                <a href="/권동환/K마이페이지.html" class="nav-item">마이페이지</a>
            </nav>
            <div class="nav-buttons">
                <% if (isLoggedIn) { %>
                    <form action="logout" method="post">
                        <button type="submit" class="btn-logout">로그아웃</button>
                    </form>
                <% } else { %>
                    <button class="btn-login">로그인</button>
                    <button class="btn-signup">회원가입</button>
                <% } %>
            </div>
        </div>
    </header>

    <div class="main-container">
        <% if (msg != null) { %>
            <div class="alert <%= (msg.equals("login_fail") ? "alert-danger" : (msg.equals("logout_success") ? "alert-info" : "alert-success")) %> show">
                <%= (msg.equals("login_fail") ? "로그인 정보가 올바르지 않습니다." : (msg.equals("logout_success") ? "로그아웃되었습니다." : "회원가입이 완료되었습니다. 로그인해주세요.")) %>
            </div>
        <% } %>
        <div class="content-sections-wrapper">
            <div class="content-section">
                <h1 class="main-title">여자들의 진짜 속마음,<br>궁금하니?</h1>
                <p class="main-description">
                    TOKKI TALK과 함께<br>
                    그녀의 메시지 속 숨은 의미를 파헤쳐보세요!
                </p>
                <button class="cta-button">
                    나랑 대화해볼래?
                    <span class="icon">💬</span>
                </button>
            </div>
            <div class="content-section">
                <h1 class="main-title">나의 연애 센스는<br>과연 몇 점일까?</h1>
                <p class="main-description">
                    TOKKI TALK의 센스고사를 통해<br>
                    당신의 숨겨진 매력과 센스를 발견해보세요!
                </p>
                <button class="cta-button">
                    센스고사 시작하기
                    <span class="icon">🚀</span>
                </button>
            </div>
        </div>
    </div>

    <footer class="footer">
        <div class="footer-container">
            <p class="footer-text">© 2024 TOKKI TALK. All Rights Reserved.</p>
        </div>
    </footer>

    <div id="loginModal" class="modal-overlay">
        <div class="modal-content">
            <button class="modal-close" onclick="closeModal('loginModal')">&times;</button>
            <h2 class="modal-title">로그인</h2>
            <form action="login" method="post" id="loginForm">
                <div class="input-group">
                    <input type="text" id="loginId" name="id" class="modal-input" placeholder="아이디를 입력하세요">
                </div>
                <div class="input-group">
                    <input type="password" id="loginPassword" name="pw" class="modal-input" placeholder="비밀번호를 입력하세요">
                </div>
                <div class="character-icons">
                    <div class="character-icon icon-cool">😎</div>
                    <div class="character-icon icon-cute">🐰</div>
                    <div class="character-icon icon-smart">🤓</div>
                </div>
                <div class="forgot-password">로그인 정보가 없으신가요?</div>
                <div class="divider"></div>
                <button type="submit" class="modal-login-btn">로그인</button>
            </form>
            <button type="button" class="modal-signup-btn">회원가입</button>
        </div>
    </div>

    <div id="signupModal" class="modal-overlay">
        <div class="signup-modal-content">
            <button class="modal-close" onclick="closeModal('signupModal')">&times;</button>
            <h2 class="signup-title">회원가입</h2>
            <div class="signup-underline"></div>
            
            <form action="join" method="post" id="signupForm">
                <div class="signup-input-row">
                    <input type="text" id="signupId" name="id" class="signup-input" placeholder="아이디 입력 ( 영문, 숫자 8자 이내 )">
                    <button type="button" class="duplicate-check-btn">중복 확인</button>
                </div>
                <div class="input-group">
                    <input type="password" id="signupPassword" name="pw" class="signup-input" placeholder="비밀번호 입력(숫자 4자)">
                </div>
                <div class="input-group">
                    <input type="password" id="signupPasswordConfirm" class="signup-input" placeholder="비밀번호 확인">
                </div>
                <div class="input-group">
                    <input type="text" id="signupName" name="name" class="signup-input" placeholder="이름을 입력해주세요">
                </div>
                <div class="input-group">
                    <input type="date" id="signupBirth" name="birth" class="date-input">
                </div>
                <div class="gender-group">
                    <input type="hidden" id="selectedGender" name="gender">
                    <div class="gender-option">
                        <div class="gender-radio" data-gender="male" onclick="selectGender(this)"></div>
                        <label class="gender-label" onclick="selectGender(this.previousElementSibling)">남자</label>
                    </div>
                    <div class="gender-option">
                        <div class="gender-radio" data-gender="female" onclick="selectGender(this)"></div>
                        <label class="gender-label" onclick="selectGender(this.previousElementSibling)">여자</label>
                    </div>
                </div>
                <div class="signup-buttons">
                    <button type="submit" class="signup-confirm-btn">회원가입 완료</button>
                    <button type="button" class="signup-cancel-btn" onclick="closeModal('signupModal')">돌아가기</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // JavaScript 코드 시작
        // 홈으로 이동 함수
        function goHome() {
            window.location.href = 'main.jsp';
        }

        // 모달 열기/닫기 함수
        function openModal(modalId) {
            document.getElementById(modalId).style.display = 'flex';
        }

        function closeModal(modalId) {
            document.getElementById(modalId).style.display = 'none';
        }

        // 성별 선택 함수
        function selectGender(element) {
            const radios = document.querySelectorAll('.gender-radio');
            radios.forEach(radio => radio.classList.remove('checked'));
            element.classList.add('checked');

            const selectedGender = element.dataset.gender;
            document.getElementById('selectedGender').value = selectedGender;
        }

        // DOM이 로드된 후 이벤트 리스너 등록
        document.addEventListener('DOMContentLoaded', function() {
            // URL 파라미터에서 메시지 가져오기 및 알림창 표시
            const urlParams = new URLSearchParams(window.location.search);
            const msg = urlParams.get('msg');
            if (msg) {
                const alertElement = document.querySelector('.alert');
                if (alertElement) {
                    alertElement.classList.add('show');
                    // 회원가입 성공 시 로그인 모달 자동 열기
                    if (msg === 'join_success') {
                        openModal('loginModal');
                    }
                }
            }
            
            // 로그인/회원가입 버튼 클릭 시 모달 열기
            document.querySelector('.btn-login').addEventListener('click', () => openModal('loginModal'));
            document.querySelector('.btn-signup').addEventListener('click', () => openModal('signupModal'));
            
            // 로그인 모달에서 회원가입 버튼 클릭 시
            document.querySelector('.modal-signup-btn').addEventListener('click', function() {
                closeModal('loginModal');
                openModal('signupModal');
            });
            
            // 모달 닫기 버튼
            document.querySelectorAll('.modal-close').forEach(button => {
                button.addEventListener('click', (e) => {
                    const modalId = e.target.closest('.modal-overlay').id;
                    closeModal(modalId);
                });
            });

            // 모달 외부 클릭시 닫기
            document.getElementById('loginModal').addEventListener('click', function(e) {
                if (e.target === this) closeModal('loginModal');
            });
            document.getElementById('signupModal').addEventListener('click', function(e) {
                if (e.target === this) closeModal('signupModal');
            });
            
            // ESC 키로 모달 닫기
            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    closeModal('loginModal');
                    closeModal('signupModal');
                }
            });
            
            // 중복 확인 버튼
            document.querySelector('.duplicate-check-btn').addEventListener('click', function() {
                alert('중복 확인을 진행합니다.');
            });

            // 회원가입 폼 제출 이벤트 리스너 (유효성 검사)
            document.getElementById('signupForm').addEventListener('submit', function(e) {
                const id = document.getElementById('signupId').value;
                const pw = document.getElementById('signupPassword').value;
                const pwConfirm = document.getElementById('signupPasswordConfirm').value;
                const name = document.getElementById('signupName').value;
                const birth = document.getElementById('signupBirth').value;
                const gender = document.getElementById('selectedGender').value;

                if (!id || !pw || !pwConfirm || !name || !birth || !gender) {
                    alert('모든 필드를 입력해주세요.');
                    e.preventDefault();
                    return;
                }
                
                if (pw !== pwConfirm) {
                    alert('비밀번호가 일치하지 않습니다.');
                    e.preventDefault();
                    return;
                }
                
                // 실제 서버 요청은 e.preventDefault() 없이 진행됩니다.
            });
        });
     // 기존 코드에 이 함수를 추가하거나, loginForm의 submit 이벤트 리스너를 수정하세요.
        function handleLoginSuccess() {
            // 1. 로그인 모달을 닫습니다.
            closeModal('loginModal'); 

            // 2. 잠시 후에 페이지를 'success.jsp'로 이동합니다.
            //    사용자가 모달이 닫히는 것을 볼 수 있도록 약간의 딜레이를 주는 것이 좋습니다.
            setTimeout(function() {
                window.location.href = 'main.jsp';
            }, 500); // 0.5초(500ms) 후에 이동
        }

     // JavaScript 코드 시작

     // 기존 함수들...

     // 로그인 성공 시 호출될 함수 (onclick 이벤트용)
	     function handleLoginSuccess() {
	         alert('로그인에 성공했습니다!');
	         
	         // 로그인 모달을 닫습니다.
	         closeModal('loginModal');
	         
	         // 1초(1000ms) 후에 'success.jsp' 페이지로 이동합니다.
	         setTimeout(function() {
	             window.location.href = 'main.jsp';
	         }, 1000);
	     }

     // 기존의 모든 이벤트 리스너들...
    </script>
</body>
</html>