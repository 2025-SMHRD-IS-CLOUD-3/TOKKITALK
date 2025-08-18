// --- 페이지 이동 제어 및 로그인 상태 확인 로직 ---
function checkLoginAndRedirect(event, pageUrl) {
    event.preventDefault(); // 기본 링크 이동 방지
    
    const loggedIn = sessionStorage.getItem('loggedIn');

    const publicPages = [
        "main.html",
        "intro.html"
    ];

    if (loggedIn !== 'true' && !publicPages.includes(pageUrl)) {
        alert("로그인이 필요한 페이지입니다. 로그인 해주세요.");
        openLoginModal();
    } else {
        window.location.href = pageUrl;
    }
}

// 메인페이지 CTA 버튼 클릭 시 로그인 여부 확인 후 페이지 이동
function handleCtaClick(event, pageUrl) {
    checkLoginAndRedirect(event, pageUrl);
}

// --- 로그인/회원가입 모달 및 UI 제어 로직 ---
const loginModal = document.getElementById('loginModal');
const signupModal = document.getElementById('signupModal');

const authButtons = document.getElementById('auth-buttons');
const userInfoArea = document.getElementById('user-info-area');
const welcomeMessage = document.querySelector('#user-info-area .welcome-text'); // welcome-text 클래스 선택
const logoutBtn = document.getElementById('logoutBtn');

function updateHeaderUI(isLoggedIn, name = '') {
    if (isLoggedIn) {
        authButtons.style.display = 'none';
        userInfoArea.style.display = 'flex';
        welcomeMessage.textContent = `${name}님 환영합니다!`;
    } else {
        authButtons.style.display = 'flex';
        userInfoArea.style.display = 'none';
    }
}

function openLoginModal() {
    loginModal.style.display = 'flex';
}

function closeLoginModal() {
    loginModal.style.display = 'none';
}

function openSignupModal() {
    signupModal.style.display = 'flex';
}

function closeSignupModal() {
    signupModal.style.display = 'none';
}

function closeAllModals() {
    closeLoginModal();
    closeSignupModal();
}

// 홈으로 이동 함수
function goHome() {
    window.location.href = "main.html";
}

// 성별 선택 함수
function selectGender(gender) {
    const radios = document.querySelectorAll('.gender-radio');
    radios.forEach(radio => radio.classList.remove('checked'));
    if (event.target.classList.contains('gender-radio')) {
        event.target.classList.add('checked');
    } else if (event.target.closest('.gender-option')) {
        event.target.closest('.gender-option').querySelector('.gender-radio').classList.add('checked');
    }
}

// URL에서 특정 파라미터 값을 가져오는 함수
function getUrlParameter(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    var results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

// DOM이 로드된 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 1. URL에서 userid 파라미터가 있는지 확인
    const useridFromUrl = getUrlParameter('userid');
    
    // 2. userid 파라미터가 있다면, sessionStorage에 저장
    if (useridFromUrl) {
        sessionStorage.setItem('loggedIn', 'true');
        sessionStorage.setItem('userName', useridFromUrl);
    }

    // 3. sessionStorage에서 로그인 상태와 사용자 이름 가져오기
    const loggedInStatus = sessionStorage.getItem('loggedIn');
    const storedUserName = sessionStorage.getItem('userName');
    
    // 4. 가져온 정보로 헤더 UI 업데이트
    updateHeaderUI(loggedInStatus === 'true', storedUserName);

    // 로그아웃 버튼 이벤트 리스너
    logoutBtn.addEventListener('click', () => {
        sessionStorage.removeItem('loggedIn');
        sessionStorage.removeItem('userName');
        alert("로그아웃되었습니다.");
        window.location.href = "main.html";
    });

    // 로그인 모달 버튼 이벤트
    const loginSubmitBtn = document.querySelector('.modal-login-btn');
    loginSubmitBtn.addEventListener('click', () => {
        // 클라이언트에서 로그인 처리 (백엔드 없이 테스트용)
        const inputId = document.querySelector('#loginModal .modal-input[placeholder="아이디를 입력하세요"]').value;
        if (inputId) {
            // 서버에 데이터를 보내고 결과를 기다리는 로직이 필요하지만,
            // 이 예제에서는 클라이언트에서 바로 처리
            sessionStorage.setItem('loggedIn', 'true');
            sessionStorage.setItem('userName', inputId);
            closeLoginModal();
            updateHeaderUI(true, inputId);
            alert(`${inputId}님, 환영합니다!`);
            window.location.reload(); // 페이지 새로고침하여 URL 파라미터 처리
        } else {
            alert('아이디를 입력해주세요.');
        }
    });

    // 회원가입 모달에서 로그인 버튼 클릭 시
    const signupModalBtn = document.querySelector('#loginModal .modal-signup-btn');
    signupModalBtn.addEventListener('click', function() {
        closeLoginModal();
        openSignupModal();
    });
    
    // 회원가입 모달에서 회원가입 완료 버튼
    const signupConfirmBtn = document.querySelector('.signup-confirm-btn');
    signupConfirmBtn.addEventListener('click', function() {
        alert('회원가입이 완료되었습니다! 🎉');
        closeSignupModal();
        openLoginModal();
    });
    
    // 모달 외부 클릭 시 닫기
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeAllModals();
            }
        });
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeAllModals();
        }
    });
});