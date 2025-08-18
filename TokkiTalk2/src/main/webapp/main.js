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
const headerUserName = document.getElementById('inputId');
const logoutBtn = document.getElementById('logoutBtn');

// 아이디 중복 확인 상태를 저장하는 전역 변수
let isIdAvailable = false;

function updateHeaderUI(isLoggedIn, name = '') {
    if (isLoggedIn) {
        authButtons.style.display = 'none';
        userInfoArea.style.display = 'flex';
        headerUserName.textContent = name; // DB에서 받아온 사용자 ID로 업데이트
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
    // 회원가입 모달 열 때 중복 확인 상태 초기화
    isIdAvailable = false;
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

    // 로그인 폼 제출 시 유효성 검사 및 서버 연동
    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault(); // 기본 폼 제출 동작 방지
        
        const idInput = this.querySelector('input[name="user_id"]');
        const pwInput = this.querySelector('input[name="user_pw"]');
        
        if (!idInput.value || !pwInput.value) {
            alert('아이디와 비밀번호를 모두 입력해주세요.');
            return;
        }

        // 폼 데이터를 URLSearchParams 객체로 변환
        const formData = new URLSearchParams(new FormData(this)).toString();

        try {
            // 서버의 로그인 엔드포인트에 POST 요청
            const res = await fetch('login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            const result = await res.json();

            if (res.ok && result.success) {
                // 로그인 성공 시 세션 스토리지에 정보 저장 및 UI 업데이트
                sessionStorage.setItem('loggedIn', 'true');
                sessionStorage.setItem('userName', result.username); // 서버에서 받은 사용자 이름 저장
                updateHeaderUI(true, result.username);
                closeAllModals(); // 모달 닫기
                alert(`${result.username}님, 환영합니다!`);
            } else {
                // 로그인 실패 시 에러 메시지 표시
                alert(result.message || '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
            }
        } catch (error) {
            console.error('Login error:', error);
            alert('로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    });

    // 회원가입 폼 제출 시 유효성 검사
    signupForm.addEventListener('submit', function(event) {
        const idInput = this.querySelector('input[name="id"]');
        const pwInput = this.querySelector('input[name="pw"]');
        const pwCheckInput = this.querySelector('input[name="user_pw_check"]');
        const nameInput = this.querySelector('input[name="name"]');
        const dateInput = this.querySelector('input[name="user_date"]');
        const genderInput = this.querySelector('input[name="gender"]:checked');
        
        // 아이디 중복 확인 여부 검사
        if (!isIdAvailable) {
            event.preventDefault();
            alert('아이디 중복 확인을 완료해주세요.');
            return;
        }

        // 간단한 필수 입력 필드 검증
        if (!idInput.value || !pwInput.value || !pwCheckInput.value || !nameInput.value || !dateInput.value || !genderInput) {
            event.preventDefault();
            alert('모든 정보를 입력해주세요.');
            return;
        }

        // 비밀번호 일치 확인
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
    
    // 로그인 모달에서 회원가입 버튼 클릭 시
    const signupModalBtn = document.querySelector('#loginModal .modal-signup-btn');
    signupModalBtn.addEventListener('click', function() {
        closeLoginModal();
        openSignupModal();
    });
    
    // 회원가입 모달에서 로그인 버튼 클릭 시
    const backToLoginBtn = document.querySelector('.modal-back-to-login');
    if (backToLoginBtn) {
        backToLoginBtn.addEventListener('click', function() {
            closeSignupModal();
            openLoginModal();
        });
    }

    // 아이디 입력 필드에 입력이 시작되면 중복 확인 상태 초기화
    const signupIdInput = document.getElementById('signupId');
    if (signupIdInput) {
        signupIdInput.addEventListener('input', function() {
            isIdAvailable = false;
        });
    }

    // 아이디 중복 확인 버튼
    const duplicateCheckBtn = document.querySelector('.duplicate-check-btn');
    if (duplicateCheckBtn) {
        duplicateCheckBtn.addEventListener('click', async function() {
            const idInput = document.getElementById('signupId');
            const userId = (idInput ? idInput.value : '').trim();
            
            if (!userId) {
                alert('아이디를 입력해주세요.');
                return;
            }

            // 아이디 유효성 검사 (영문/숫자 1~8자)
            const idRegex = /^[A-Za-z0-9]{1,8}$/;
            if (!idRegex.test(userId)) {
                alert('아이디는 영문/숫자 1~8자만 가능합니다.');
                return;
            }

            try {
                // 서버의 /check-duplicate 경로로 GET 요청 전송
                // 실제 서버 URL로 변경해야 합니다. 예: '/tokkitalk/check-duplicate?id='
                const res = await fetch('check-duplicate?id=' + encodeURIComponent(userId), { method: 'GET' });
                
                if (!res.ok) {
                    throw new Error('Server response was not ok.');
                }
                
                const data = await res.json();
                
                if (data.exists) {
                    alert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
                    isIdAvailable = false;
                } else {
                    alert('사용 가능한 아이디입니다!');
                    isIdAvailable = true;
                }
            } catch (e) {
                alert('중복 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                console.error(e);
                isIdAvailable = false;
            }
        });
    }
    
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
