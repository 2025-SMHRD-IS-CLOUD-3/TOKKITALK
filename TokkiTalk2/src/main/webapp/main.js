/**
 * @fileoverview 이 파일은 TOKKI TALK 웹 애플리케이션의 메인 페이지를 위한 JavaScript 코드를 포함합니다.
 * 사용자 인증 상태를 확인하고, 모달 창을 제어하며, UI 상호작용을 처리합니다.
 */

// --- 페이지 이동 제어 및 로그인 상태 확인 로직 ---
/**
 * @description 로그인 상태를 확인하고 페이지를 리디렉션합니다.
 * @param {Event} event - 클릭 이벤트 객체
 * @param {string} pageUrl - 이동할 페이지 URL
 */
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

/**
 * @description 메인페이지 CTA 버튼 클릭 시 로그인 여부 확인 후 페이지 이동을 처리합니다.
 * @param {Event} event - 클릭 이벤트 객체
 * @param {string} pageUrl - 이동할 페이지 URL
 */
function handleCtaClick(event, pageUrl) {
    checkLoginAndRedirect(event, pageUrl);
}

// --- 로그인/회원가입 모달 및 UI 제어 로직 ---
const loginModal = document.getElementById('loginModal');
const signupModal = document.getElementById('signupModal');
const authButtons = document.getElementById('auth-buttons');
const userInfoArea = document.getElementById('user-info-area');
const welcomeMessage = document.querySelector('#user-info-area .welcome-text');
const logoutBtn = document.getElementById('logoutBtn');

/**
 * @description 헤더 UI를 업데이트하여 로그인 상태를 반영합니다.
 * @param {boolean} isLoggedIn - 로그인 여부
 * @param {string} [name=''] - 로그인한 사용자의 이름
 */
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

/** 모달 열고 닫기 */
function openLoginModal() { loginModal.style.display = 'flex'; }
function closeLoginModal() { loginModal.style.display = 'none'; }
function openSignupModal() { signupModal.style.display = 'flex'; }
function closeSignupModal() { signupModal.style.display = 'none'; }
function closeAllModals() { closeLoginModal(); closeSignupModal(); }

/** 홈으로 이동 */
function goHome() { window.location.href = "main.html"; }

/**
 * @description 성별 라디오 버튼을 선택합니다.
 * @param {string} gender - 'male' 또는 'female'
 */
function selectGender(gender) {
    const allOptions = document.querySelectorAll('.gender-option');
    allOptions.forEach(option => {
        const radio = option.querySelector('.gender-radio');
        radio.classList.remove('checked');
    });

    const selectedOption = document.querySelector(`.gender-option[data-gender="${gender}"]`);
    if (selectedOption) {
        const selectedRadio = selectedOption.querySelector('.gender-radio');
        selectedRadio.classList.add('checked');
    }
}

/** URL 파라미터 */
function getUrlParameter(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    var results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

/** 컨텍스트 경로 */
function getContextPath() {
    var pathSegments = window.location.pathname.split('/').filter(function(seg){ return seg.length > 0; });
    if (pathSegments.length === 0) return '';
    if (pathSegments[0].indexOf('.') !== -1) return '';
    return '/' + pathSegments[0];
}

/** API 베이스 URL */
function getApiBase() {
    var basePath = getContextPath();
    if (basePath) return window.location.origin + basePath;
    var host = window.location.hostname;
    if (host === '127.0.0.1' || host === 'localhost') return 'http://localhost:8081/TokkiTalk2';
    return window.location.origin;
}

// DOM이 로드된 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const modalType = urlParams.get('modal');

    if (modalType === 'login') openLoginModal();
    else if (modalType === 'signup') openSignupModal();
		
    // 로그인 상태 처리
    const useridFromUrl = getUrlParameter('userid');
    if (useridFromUrl) {
        sessionStorage.setItem('loggedIn', 'true');
        sessionStorage.setItem('userName', useridFromUrl);
    }

    const loggedInStatus = sessionStorage.getItem('loggedIn');
    const storedUserName = sessionStorage.getItem('userName');
    updateHeaderUI(loggedInStatus === 'true', storedUserName);

    // 로그아웃 버튼 이벤트
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');
            alert("로그아웃되었습니다.");
            window.location.href = "main.html";
        });
    }

    // 로그인 버튼 이벤트
    const loginSubmitBtn = document.querySelector('.modal-login-btn');
    if (loginSubmitBtn) {
        loginSubmitBtn.addEventListener('click', async () => {
            const idInputEl = document.querySelector('#loginModal .modal-input[placeholder="아이디를 입력하세요"]');
            const pwInputEl = document.querySelector('#loginModal .modal-input[placeholder="비밀번호를 입력하세요"]');
            const inputId = idInputEl ? idInputEl.value.trim() : '';
            const inputPw = pwInputEl ? pwInputEl.value.trim() : '';
            if (!inputId || !inputPw) {
                alert('아이디와 비밀번호를 모두 입력해주세요.');
                return;
            }
            try {
                const url = getApiBase() + '/login';
                const res = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'Accept': 'application/json'
                    },
                    body: new URLSearchParams({ id: inputId, pw: inputPw, ajax: '1' }).toString()
                });
                if (!res.ok) {
                    alert('로그인에 실패했습니다.');
                    return;
                }
                const data = await res.json();
                if (data && data.success) {
                    const nameToUse = data.userName || data.userId || inputId;
                    sessionStorage.setItem('loggedIn', 'true');
                    sessionStorage.setItem('userId', data.userId);
                    sessionStorage.setItem('userName', data.userName);
                    alert(`${nameToUse}님, 환영합니다!`);
                    updateHeaderUI(true, nameToUse);
                    closeLoginModal();
                    window.location.href = "main.html";
                } else {
                    alert('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
                }
            } catch (e) {
                alert('네트워크 오류가 발생했습니다.');
            }
        });
    }

    // 회원가입 모달에서 로그인 버튼 → 회원가입 모달 열기
    const signupModalBtn = document.querySelector('#loginModal .modal-signup-btn');
    if (signupModalBtn) {
        signupModalBtn.addEventListener('click', function() {
            closeLoginModal();
            openSignupModal();
        });
    }
    
    // 회원가입 완료 버튼
    const signupConfirmBtn = document.querySelector('.signup-confirm-btn');
    if (signupConfirmBtn) {
        signupConfirmBtn.addEventListener('click', async function() {
            const idInputEl = document.querySelector('#signupModal .signup-input-row .signup-input');
            const pw1El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 입력(숫자 4자)"]');
            const pw2El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 확인"]');
            const nameEl = document.querySelector('#signupModal .signup-input[placeholder="이름을 입력해주세요"]');
            const dateEl = document.querySelector('#signupModal .date-input');

            // ✅ 성별 선택 요소 가져오기 (수정된 부분)
            const genderChecked = document.querySelector('#signupModal .gender-option .gender-radio.checked');
            let gender = '';
            if (genderChecked) {
                const optionEl = genderChecked.closest('.gender-option');
                if (optionEl) gender = optionEl.dataset.gender || '';
            }

            const userId = idInputEl ? idInputEl.value.trim() : '';
            const userPw1 = pw1El ? pw1El.value.trim() : '';
            const userPw2 = pw2El ? pw2El.value.trim() : '';
            const userName = nameEl ? nameEl.value.trim() : '';
            const userDate = dateEl ? dateEl.value : '';

            if (!/^[A-Za-z0-9]{1,20}$/.test(userId)) {
                alert('아이디는 영문/숫자 1~20자만 가능합니다.');
                return;
            }
            if (!/^\d{4}$/.test(userPw1)) {
                alert('비밀번호는 숫자 4자리여야 합니다.');
                return;
            }
            if (userPw1 !== userPw2) {
                alert('비밀번호가 일치하지 않습니다.');
                return;
            }
            if (!userName) {
                alert('이름을 입력해주세요.');
                return;
            }
            if (!userDate) {
                alert('생년월일을 선택해주세요.');
                return;
            }
            if (!gender) {
                alert('성별을 선택해주세요.');
                return;
            }

            try {
                const url = getApiBase() + '/join';
                const res = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'Accept': 'application/json'
                    },
                    body: new URLSearchParams({
                        id: userId,
                        pw: userPw1,
                        name: userName,
                        gender: gender,
                        user_date: userDate,
                        ajax: '1'
                    }).toString()
                });
                if (!res.ok) {
                    alert('회원가입 처리 중 오류가 발생했습니다.');
                    return;
                }
                const data = await res.json();
                if (data && data.success) {
                    alert('회원가입이 완료되었습니다! 🎉');
                    // 입력 필드 초기화
                    if (idInputEl) idInputEl.value = '';
                    if (pw1El) pw1El.value = '';
                    if (pw2El) pw2El.value = '';
                    if (nameEl) nameEl.value = '';
                    if (dateEl) dateEl.value = '';
                    document.querySelectorAll('#signupModal .gender-radio').forEach(radio => radio.classList.remove('checked'));

                    closeAllModals(); 
                    openLoginModal();
                } else if (data && data.reason === 'duplicate') {
                    alert('이미 사용 중인 아이디입니다.');
                } else {
                    alert('회원가입에 실패했습니다.');
                }
            } catch (e) {
                alert('네트워크 오류가 발생했습니다.');
            }
        });
    }

    // 아이디 중복 확인 버튼
    const duplicateCheckBtn = document.querySelector('.duplicate-check-btn');
    if (duplicateCheckBtn) {
        duplicateCheckBtn.addEventListener('click', async function() {
            const idInput = document.querySelector('#signupModal .signup-input-row .signup-input');
            const userId = (idInput ? idInput.value : '').trim();
            if (!userId) {
                alert('아이디를 입력해주세요.');
                return;
            }
            if (!/^[A-Za-z0-9]{1,20}$/.test(userId)) {
                alert('아이디는 영문/숫자 1~20자만 가능합니다.');
                return;
            }
            try {
                const url = getApiBase() + '/check-duplicate?id=' + encodeURIComponent(userId);
                const res = await fetch(url, { method: 'GET' });
                if (!res.ok) {
                    alert('중복 확인 중 오류가 발생했습니다.');
                    return;
                }
                const data = await res.json();
                if (data.exists) {
                    alert('이미 사용 중인 아이디입니다.');
                } else {
                    alert('사용 가능한 아이디입니다!');
                }
            } catch (e) {
                alert('네트워크 오류가 발생했습니다.');
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

    // gender-option 클릭 시 라디오 버튼 선택
    document.querySelectorAll('.gender-option').forEach(option => {
        option.addEventListener('click', function() {
            const gender = this.dataset.gender;
            selectGender(gender);
        });
    });
});
