// main.js

// --- 페이지 이동 제어 및 로그인 상태 확인 로직 ---
function checkLoginAndRedirect(event, pageUrl) {
    event.preventDefault(); // 기본 링크 이동 방지
    
    const loggedIn = sessionStorage.getItem('loggedIn');

    const publicPages = [
        "main.html",
        "intro.html"
    ];

    if (loggedIn !== 'true' && !publicPages.includes(pageUrl)) {
        alert("로그인이 필요한 페이지입니다. 로그인해주세요.");
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
const welcomeMessage = document.querySelector('#user-info-area .welcome-text');
const headerUserName = document.getElementById('inputId'); // 사용자 이름 표시를 위한 요소
const logoutBtn = document.getElementById('logoutBtn');

function updateHeaderUI(isLoggedIn, name = '') {
    if (isLoggedIn) {
        authButtons.style.display = 'none';
        userInfoArea.style.display = 'flex';
        headerUserName.textContent = name;
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
function selectGender(genderId) {
    const radios = document.querySelectorAll('.gender-radio');
    radios.forEach(radio => radio.classList.remove('checked'));
    document.getElementById(genderId).checked = true; // 실제 라디오 버튼 선택
    document.getElementById(genderId).nextElementSibling.classList.add('checked'); // 커스텀 UI 업데이트
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
    // URL 파라미터에서 로그인 성공 메시지 확인
    const loggedInFromUrl = getUrlParameter('loggedIn');
    const useridFromUrl = getUrlParameter('userid');

    if (loggedInFromUrl === 'true' && useridFromUrl) {
        sessionStorage.setItem('loggedIn', 'true');
        sessionStorage.setItem('userName', useridFromUrl);
        // 사용자에게 성공 알림
        alert(`${useridFromUrl}님, 환영합니다!`);
        // URL에서 파라미터 제거
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 세션에서 로그인 상태와 사용자 이름 가져오기
    const loggedInStatus = sessionStorage.getItem('loggedIn');
    const storedUserName = sessionStorage.getItem('userName');
    
    // 가져온 정보로 헤더 UI 업데이트
    updateHeaderUI(loggedInStatus === 'true', storedUserName);

    // --- 로그인/회원가입 폼 제출 이벤트 리스너 ---
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');

    // 로그인 폼 제출 시 유효성 검사
    loginForm.addEventListener('submit', function(event) {
        const idInput = this.querySelector('input[name="user_id"]');
        const pwInput = this.querySelector('input[name="user_pw"]');
        
        if (!idInput.value || !pwInput.value) {
            event.preventDefault();
            alert('아이디와 비밀번호를 모두 입력해주세요.');
        }
    });

	// 회원가입 폼 제출 시 유효성 검사
	signupForm.addEventListener('submit', function(event) {
	    const idInput = this.querySelector('input[name="user_id"]');
	    const pwInput = this.querySelector('input[name="user_pw"]');
	    const pwCheckInput = this.querySelector('input[name="user_pw_check"]'); // **추가된 부분**
	    const nameInput = this.querySelector('input[name="user_name"]');
	    const dateInput = this.querySelector('input[name="user_date"]');
	    const genderInput = this.querySelector('input[name="gender"]:checked');

	    // 간단한 필수 입력 필드 검증
	    if (!idInput.value || !pwInput.value || !pwCheckInput.value || !nameInput.value || !dateInput.value || !genderInput) {
	        event.preventDefault();
	        alert('모든 정보를 입력해주세요.');
	        return;
	    }

	    // 비밀번호 일치 확인 **수정된 부분**
	    if (pwInput.value !== pwCheckInput.value) {
	        event.preventDefault();
	        alert('비밀번호가 일치하지 않습니다.');
	        return;
	    }
	});

    // 로그아웃 버튼 이벤트 리스너
    logoutBtn.addEventListener('click', () => {
        sessionStorage.removeItem('loggedIn');
        sessionStorage.removeItem('userName');
        alert("로그아웃되었습니다.");
        window.location.href = "main.html";
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