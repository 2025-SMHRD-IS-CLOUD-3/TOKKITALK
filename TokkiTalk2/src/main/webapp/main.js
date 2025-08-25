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
        // alert() 대신 커스텀 모달 또는 메시지 박스를 사용합니다.
        // 현재는 편의상 alert을 사용하지만, 실제 프로덕션에서는 UI를 구현해야 합니다.
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
const welcomeMessage = document.querySelector('#user-info-area .welcome-text'); // welcome-text 클래스 선택
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

/**
 * @description 로그인 모달을 엽니다.
 */
function openLoginModal() {
    loginModal.style.display = 'flex';
}

/**
 * @description 로그인 모달을 닫습니다.
 */
function closeLoginModal() {
    loginModal.style.display = 'none';
}

/**
 * @description 회원가입 모달을 엽니다.
 */
function openSignupModal() {
    signupModal.style.display = 'flex';
}

/**
 * @description 회원가입 모달을 닫습니다.
 */
function closeSignupModal() {
    signupModal.style.display = 'none';
}

/**
 * @description 모든 모달을 닫습니다.
 */
function closeAllModals() {
    closeLoginModal();
    closeSignupModal();
}

/**
 * @description 홈페이지로 이동합니다.
 */
function goHome() {
    window.location.href = "main.html";
}

/**
 * @description 성별 라디오 버튼을 선택합니다.
 * @param {string} gender - 'male' 또는 'female'
 */
function selectGender(gender) {
    const radios = document.querySelectorAll('.gender-radio');
    radios.forEach(radio => radio.classList.remove('checked'));
    if (gender === 'male') {
        document.querySelector('.gender-option:first-child .gender-radio').classList.add('checked');
    } else {
        document.querySelector('.gender-option:last-child .gender-radio').classList.add('checked');
    }
}


/**
 * @description URL에서 특정 파라미터 값을 가져오는 함수
 * @param {string} name - 가져올 파라미터 이름
 * @returns {string} - 파라미터 값
 */
function getUrlParameter(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    var results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

/**
 * @description 현재 배포 컨텍스트 경로를 계산 (예: '/TokkiTalk2' 또는 '/')
 * @returns {string} - 컨텍스트 경로
 */
function getContextPath() {
    var pathSegments = window.location.pathname.split('/').filter(function(seg){ return seg.length > 0; });
    if (pathSegments.length === 0) {
        return '';
    }
    // 첫 세그먼트가 파일명(예: main.html)인 경우 컨텍스트 경로 없음으로 처리
    if (pathSegments[0].indexOf('.') !== -1) {
        return '';
    }
    return '/' + pathSegments[0];
}

/**
 * @description API 베이스 URL 계산: 동일 도메인 컨텍스트 우선, 없으면 로컬 톰캣(8081)로 폴백
 * @returns {string} - API 베이스 URL
 */
function getApiBase() {
    var basePath = getContextPath();
    if (basePath) {
        return window.location.origin + basePath;
    }
    var host = window.location.hostname;
    if (host === '127.0.0.1' || host === 'localhost') {
        return 'http://localhost:8081/TokkiTalk2';
    }
    return window.location.origin; // 최후수단
}

// DOM이 로드된 후 이벤트 리스너 등록
/**
 * @description DOM 로드 완료 시 실행되는 초기화 함수
 * @async
 */
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const modalType = urlParams.get('modal');

    if (modalType === 'login') {
        openLoginModal();
    } else if (modalType === 'signup') {
        openSignupModal();
    }
		
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
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');
            alert("로그아웃되었습니다.");
            window.location.href = "main.html";
        });
    }

    // 로그인 모달 버튼 이벤트
    const loginSubmitBtn = document.querySelector('.modal-login-btn');
    if (loginSubmitBtn) {
        /**
         * @description 로그인 버튼 클릭 이벤트 핸들러
         * @async
         */
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
                    alert('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
                    return;
                }
                const data = await res.json();
                if (data && data.success) {
                    const nameToUse = data.userName || data.userId || inputId;
                        sessionStorage.setItem('loggedIn', 'true');
                        sessionStorage.setItem('userId', data.userId);
                        sessionStorage.setItem('userName', data.userName);
                        
                        // 알림창을 먼저 띄우고 사용자가 확인을 누른 후 모든 동작을 수행
                        alert(`${nameToUse}님, 환영합니다!`);
                        updateHeaderUI(true, nameToUse);
                        closeLoginModal(); // 모달을 닫음
                        window.location.href = "main.html"; // ★ 이 코드로 변경합니다.
                } else {
                    alert('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
                }
            } catch (e) {
                alert('네트워크 오류가 발생했습니다.');
            }
        });
    }

    // 회원가입 모달에서 로그인 버튼 클릭 시
    const signupModalBtn = document.querySelector('#loginModal .modal-signup-btn');
    if (signupModalBtn) {
        signupModalBtn.addEventListener('click', function() {
            closeLoginModal();
            openSignupModal();
        });
    }
    
    // 회원가입 모달에서 회원가입 완료 버튼
    const signupConfirmBtn = document.querySelector('.signup-confirm-btn');
    if (signupConfirmBtn) {
        signupConfirmBtn.addEventListener('click', async function() {
            const idInputEl = document.querySelector('#signupModal .signup-input-row .signup-input');
            const pw1El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 입력(숫자 4자)"]');
            const pw2El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 확인"]');
            const nameEl = document.querySelector('#signupModal .signup-input[placeholder="이름을 입력해주세요"]');
            const dateEl = document.querySelector('#signupModal .date-input');
            const genderChecked = document.querySelector('#signupModal .gender-radio.checked');

            const userId = idInputEl ? idInputEl.value.trim() : '';
            const userPw1 = pw1El ? pw1El.value.trim() : '';
            const userPw2 = pw2El ? pw2El.value.trim() : '';
            const userName = nameEl ? nameEl.value.trim() : '';
            const userDate = dateEl ? dateEl.value : '';
            let gender = '';
            if (genderChecked) {
                const labelEl = genderChecked.parentElement.querySelector('.gender-label');
                const text = labelEl ? labelEl.textContent.trim() : '';
                gender = text.startsWith('남') ? 'male' : (text.startsWith('여') ? 'female' : '');
            }

            const idRegex = /^[A-Za-z0-9]{1,20}$/;
            if (!idRegex.test(userId)) {
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
                    alert('회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                    return;
                }
                const data = await res.json();
                if (data && data.success) {
                    alert('회원가입이 완료되었습니다! 🎉');
                    // ★ 입력 필드를 초기화하는 코드를 추가합니다.
                    const idInputEl = document.querySelector('#signupModal .signup-input-row .signup-input');
                    const pw1El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 입력(숫자 4자)"]');
                    const pw2El = document.querySelector('#signupModal .signup-input[placeholder="비밀번호 확인"]');
                    const nameEl = document.querySelector('#signupModal .signup-input[placeholder="이름을 입력해주세요"]');
                    const dateEl = document.querySelector('#signupModal .date-input');
                    const genderRadios = document.querySelectorAll('#signupModal .gender-radio');

                    // 입력창 비우기
                    if (idInputEl) idInputEl.value = '';
                    if (pw1El) pw1El.value = '';
                    if (pw2El) pw2El.value = '';
                    if (nameEl) nameEl.value = '';
                    if (dateEl) dateEl.value = '';

                    // 성별 선택 초기화
                    genderRadios.forEach(radio => radio.classList.remove('checked'));

                    closeAllModals(); 
                    openLoginModal();
                } else if (data && data.reason === 'duplicate') {
                    alert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
                } else {
                    alert('회원가입에 실패했습니다. 입력 정보를 확인해주세요.');
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
            const idRegex = /^[A-Za-z0-9]{1,20}$/;
            if (!idRegex.test(userId)) {
                alert('아이디는 영문/숫자 1~20자만 가능합니다.');
                return;
            }
            try {
                const url = getApiBase() + '/check-duplicate?id=' + encodeURIComponent(userId);
                const res = await fetch(url, { method: 'GET' });
                if (!res.ok) {
                    if (res.status === 404 || res.status === 0) {
                        alert('중복 확인 API에 연결할 수 없습니다. Tomcat에서 페이지를 열어주세요: http://localhost:8081/TokkiTalk2/main.html');
                    } else {
                        alert('중복 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                    }
                    return;
                }
                const data = await res.json();
                if (data.exists) {
                    alert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
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
            const radio = this.querySelector('.gender-radio');
            selectGender(radio.dataset.gender);
        });
    });
});
