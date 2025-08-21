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

// 현재 배포 컨텍스트 경로를 계산 (예: '/TokkiTalk2' 또는 '/')
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

// API 베이스 URL 계산: 동일 도메인 컨텍스트 우선, 없으면 로컬 톰캣(8081)로 폴백
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
				sessionStorage.setItem('userId', data.userId); // ★ 아이디를 'userId' 키에 저장
				sessionStorage.setItem('userName', data.userName); // ★ 이름을 'userName' 키에 저장
                closeLoginModal();
                updateHeaderUI(true, nameToUse);
                alert(`${nameToUse}님, 환영합니다!`);
                window.location.reload();
            } else {
                alert('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
            }
        } catch (e) {
            alert('네트워크 오류가 발생했습니다.');
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
                closeSignupModal();
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
});
