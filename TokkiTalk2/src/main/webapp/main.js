// --- 페이지 이동 제어 및 로그인 상태 확인 로직 ---
function checkLoginAndRedirect(event, pageUrl) {
    event.preventDefault(); // 기본 링크 이동 방지
    
    const loggedIn = sessionStorage.getItem('loggedIn');

    // 메인 페이지와 소개 페이지는 로그인 없이 접근 가능
    const publicPages = [
        "main.html",
        "intro.html"
    ];

    // 만약 로그인 상태가 아니면서, public 페이지가 아닌 곳으로 이동하려 할 때
    if (loggedIn !== 'true' && !publicPages.includes(pageUrl)) {
        alert("로그인이 필요한 페이지입니다. 로그인 해주세요.");
        openLoginModal(); // 로그인 모달을 열어줌
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
const welcomeText = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');

function updateHeaderUI(isLoggedIn, name = '') {
    if (isLoggedIn) {
        authButtons.style.display = 'none';
        userInfoArea.style.display = 'flex';
        welcomeText.textContent = name;
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

// DOM이 로드된 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    const loggedInStatus = sessionStorage.getItem('loggedIn');
    const storedUserName = sessionStorage.getItem('userName');
    
    // 로그인 상태에 따라 UI 업데이트
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
        const inputId = document.querySelector('#loginModal .modal-input[placeholder="아이디를 입력하세요"]').value;
        if (inputId) {
            sessionStorage.setItem('loggedIn', 'true');
            sessionStorage.setItem('userName', inputId);
            closeLoginModal();
            updateHeaderUI(true, inputId);
            alert(`${inputId}님, 환영합니다!`);
            window.location.reload();
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