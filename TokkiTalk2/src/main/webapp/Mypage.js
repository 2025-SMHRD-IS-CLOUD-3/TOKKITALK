const chatListElement = document.getElementById('chatList');
const userNameElement = document.getElementById('userName');
const openProfileModalBtn = document.getElementById('openProfileModal');
const deleteAccountBtn = document.getElementById('deleteAccountBtn');

const chatModal = document.getElementById("chatModal");
const modalContentArea = document.getElementById("modalContent");
const chatCloseBtn = document.querySelector(".chat-close-btn");

const profileModal = document.getElementById("profileModal");
const profileCloseBtn = document.querySelector(".profile-close-btn");
const editNameInput = document.getElementById("editName");
const currentPasswordInput = document.getElementById("currentPassword");
const editPasswordInput = document.getElementById("editPassword");
const confirmPasswordInput = document.getElementById("confirmPassword");
const saveProfileBtn = document.getElementById("saveProfileBtn");

const authButtons = document.getElementById('auth-buttons');
const userInfoArea = document.getElementById('user-info-area');
const headerUserName = document.getElementById('headerUserName');
const logoutBtn = document.getElementById('logoutBtn');

function updateProfileDisplay(profile) {
    userNameElement.textContent = `${profile.name}님`;
    headerUserName.textContent = profile.name;
}

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

function checkLoginState() {
    const loggedIn = sessionStorage.getItem('loggedIn');
    if (loggedIn !== 'true') {
        alert('로그인이 필요합니다.');
        window.location.href = 'main.html';
    }
}

function getApiBase() {
    const segs = window.location.pathname.split('/').filter(s => s.length>0);
    if (segs.length>0 && segs[0].indexOf('.')===-1) return window.location.origin + '/' + segs[0];
    const host = window.location.hostname;
    if (host==='127.0.0.1' || host==='localhost') return 'http://localhost:8081/TokkiTalk2';
    return window.location.origin;
}

/**
 * @description 사용자의 채팅 히스토리 목록을 가져오는 함수
 * @async
 * @returns {Promise<void>}
 */
async function fetchChatList() {
    try {
        // 서버가 세션으로 사용자 식별 → 파라미터 없이 호출
        const url = getApiBase() + '/getHistory';
        const res = await fetch(url);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const list = await res.json();
        const chats = list.map(r => ({
            id: r.id,
            title: r.inputText ? (r.inputText.length>40 ? r.inputText.substring(0,40)+'…' : r.inputText) : '(무제)',
            date: r.createdAt ? new Date(r.createdAt).toLocaleString() : ''
        }));
        renderChatList(chats);
    } catch (e) {
        console.error('Error fetching chat list:', e);
        chatListElement.innerHTML = '<li class="loading-message">불러오기 실패</li>';
    }
}

/**
 * @description 채팅 목록을 DOM에 렌더링하는 함수
 * @param {Array} chats - 채팅 데이터 배열
 */
function renderChatList(chats) {
    chatListElement.innerHTML = '';
    if (chats.length === 0) {
        chatListElement.innerHTML = '<li class="loading-message">대화 기록이 없습니다.</li>';
        return;
    }
    chats.forEach(chat => {
        const li = document.createElement('li');
        li.innerHTML = `
            <a href="#" class="list-item-link" data-chat-id="${chat.id}">
                <span class="list-item-title">${chat.title}</span>
                <span class="list-item-date">${chat.date}</span>
            </a>
        `;
        chatListElement.appendChild(li);
    });

    document.querySelectorAll('.list-item-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const chatId = this.getAttribute('data-chat-id');
            fetchChatDetails(chatId);
        });
    });
}

async function fetchChatDetails(chatId) {
    try {
        modalContentArea.innerHTML = '<div class="loading-message">불러오는 중...</div>';
        chatModal.style.display = "flex";
        
        // getHistory에서 전체 목록을 가져와서 해당 ID의 항목을 찾기
        const url = getApiBase() + '/getHistory';
        const res = await fetch(url);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        
        const list = await res.json();
        const chatData = list.find(item => item.id == chatId);
        
        if (!chatData) {
            modalContentArea.innerHTML = '<div class="error-message">해당 대화를 찾을 수 없습니다.</div>';
            return;
        }
        
        // advice 배열을 렌더링
        let adviceHtml = '';
        if (chatData.advice && Array.isArray(chatData.advice)) {
            adviceHtml = chatData.advice.map(advice => {
                return `<li><b>${advice.style || '제안'}</b> ${advice.text || ''}</li>`;
            }).join('');
        }
        
        modalContentArea.innerHTML = `
            <div class="chat-detail">
                <h3>분석 결과</h3>
                <div class="analysis-section">
                    <h4>입력 문장</h4>
                    <p>${chatData.inputText || '분석 없음'}</p>
                </div>
                <div class="analysis-section">
                    <h4>표면적 의미</h4>
                    <p>${chatData.surfaceMeaning || '분석 없음'}</p>
                </div>
                <div class="analysis-section">
                    <h4>숨은 의도</h4>
                    <p>${chatData.hiddenMeaning || '분석 없음'}</p>
                </div>
                <div class="analysis-section">
                    <h4>감정 상태</h4>
                    <p>${chatData.emotionLabel || '분석 없음'} (강도: ${chatData.emotionIntensity || 'N/A'})</p>
                </div>
                <div class="analysis-section">
                    <h4>직설 번역</h4>
                    <p>${chatData.translation || '분석 없음'}</p>
                </div>
                <div class="analysis-section">
                    <h4>TOKKI의 제안</h4>
                    <ul class="advice-list">
                        ${adviceHtml}
                    </ul>
                </div>
            </div>
        `;
    } catch (e) {
        modalContentArea.innerHTML = '<div class="error-message">불러오기 실패</div>';
        console.error('Error fetching chat details:', e);
    }
}

if (openProfileModalBtn) {
    openProfileModalBtn.addEventListener('click', function() {
        const currentName = sessionStorage.getItem('userName');
        editNameInput.value = currentName; // ★★★ 이 코드가 실제 이름을 입력합니다. ★★★
        currentPasswordInput.value = "";
        editPasswordInput.value = "";
        confirmPasswordInput.value = "";
        profileModal.style.display = "flex";
    });
}

if (saveProfileBtn) {
    saveProfileBtn.addEventListener('click', async function(event) {
        event.preventDefault();

        const user_id = sessionStorage.getItem('userId');
        const current_pw = currentPasswordInput.value.trim();
        const new_pw = editPasswordInput.value.trim();
        const confirm_pw = confirmPasswordInput.value.trim();
        
        if (!user_id) {
            alert('로그인 정보가 유효하지 않습니다.');
            return;
        }

        if (!current_pw || !new_pw || !confirm_pw) {
            alert('현재 비밀번호와 새 비밀번호를 모두 입력해주세요.');
            return;
        }

        if (new_pw !== confirm_pw) {
            alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            return;
        }

        try {
            const url = '/TokkiTalk2/Update';
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: `id=${encodeURIComponent(user_id)}&currentPw=${encodeURIComponent(current_pw)}&newPw=${encodeURIComponent(new_pw)}`
            });

            if (!response.ok) {
                alert('서버와의 통신 오류가 발생했습니다.');
                return;
            }
            
            const result = await response.json();

            if (result.success) {
                alert(result.message);
                window.location.href = 'MyPage.html'; // ★★★ 성공 시 페이지 이동 ★★★
            } else {
                alert(result.message);
            }
            
        } catch (error) {
            console.error('Error during password update:', error);
            alert('네트워크 오류가 발생했습니다.');
        }
    });
}

if (deleteAccountBtn) {
    deleteAccountBtn.addEventListener('click', async function() {
        if (confirm("정말 회원 탈퇴하시겠습니까? 모든 정보가 삭제됩니다.")) {
            // ★ 수정된 부분: sessionStorage에서 'userId'를 가져와야 합니다.
            // 현재 로그인 로직이 'userName'만 저장하므로, 임시로 'userName'을 사용합니다.
            // 로그인 로직 수정이 필요합니다.
            const userId = sessionStorage.getItem('userId');
            
            if (!userId) {
                alert('로그인 정보가 유효하지 않습니다.');
                window.location.href = 'main.html';
                return;
            }

            try {
                // 서버의 Delete 컨트롤러로 POST 요청 보내기
                const url = '/TokkiTalk2/Delete';
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: `id=${encodeURIComponent(userId)}`
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        sessionStorage.removeItem('loggedIn');
                        sessionStorage.removeItem('userName');
                        alert('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
                        window.location.href = 'main.html';
                    } else {
                        alert('회원 탈퇴에 실패했습니다: ' + (result.message || '알 수 없는 오류'));
                    }
                } else {
                    alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
                }
            } catch (error) {
                console.error('Error during account deletion:', error);
                alert('네트워크 오류가 발생했습니다.');
            }
        }
    });
}

if (chatCloseBtn) {
    chatCloseBtn.onclick = function() {
        chatModal.style.display = "none";
    };
}

if (profileCloseBtn) {
    profileCloseBtn.onclick = function() {
        profileModal.style.display = "none";
    };
}

window.onclick = function(event) {
    if (event.target == chatModal) {
        chatModal.style.display = "none";
    }
    if (event.target == profileModal) {
        profileModal.style.display = "none";
    }
};

if (logoutBtn) {
    logoutBtn.addEventListener('click', function() {
        sessionStorage.removeItem('loggedIn');
        sessionStorage.removeItem('userName');
        alert('로그아웃 되었습니다.');
        window.location.href = 'main.html';
    });
}

window.addEventListener('load', function() {
    const storedUserName = sessionStorage.getItem('userName');
    const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true';

    updateHeaderUI(isLoggedIn, storedUserName);
    updateProfileDisplay({ name: storedUserName });
    
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('msg');

    if (msg) {
        switch(msg) {
            case 'update_success':
                alert('프로필이 성공적으로 업데이트되었습니다.');
                break;
            case 'update_fail':
                alert('프로필 업데이트에 실패했습니다. 다시 시도해 주세요.');
                break;
            case 'password_mismatch':
                alert('현재 비밀번호가 일치하지 않습니다. 다시 확인해주세요.');
                break;
            case 'not_logged_in':
                alert('로그인이 필요한 서비스입니다.');
                break;
        }
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

document.querySelectorAll('.nav-item').forEach(link => {
    link.addEventListener('click', (e) => {
        const href = e.target.getAttribute('href');
        if (href === 'intro.html' || href === 'main.html') {
            return;
        } else {
            e.preventDefault();
            checkLoginState();
            if (sessionStorage.getItem('loggedIn') === 'true') {
                window.location.href = href;
            }
        }
    });
});

// 페이지 로드 시 채팅 목록 가져오기
fetchChatList();