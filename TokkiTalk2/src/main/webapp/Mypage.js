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

function fetchChatList() {
    const chats = [];
    renderChatList(chats);
}

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

function fetchChatDetails(chatId) {
    modalContentArea.innerHTML = '<div class="loading-message">대화 내용을 불러올 수 없습니다.</div>';
    chatModal.style.display = "flex";
}

if (openProfileModalBtn) {
    openProfileModalBtn.addEventListener('click', function() {
        const currentName = sessionStorage.getItem('userName');
        editNameInput.value = currentName;
        currentPasswordInput.value = "";
        editPasswordInput.value = "";
        confirmPasswordInput.value = "";
        profileModal.style.display = "flex";
    });
}

if (saveProfileBtn) {
    saveProfileBtn.addEventListener('click', function(event) {
        event.preventDefault();

        const user_id = sessionStorage.getItem('userName');
        const user_pw = currentPasswordInput.value.trim();
        const user_name = editNameInput.value.trim();

        if (!user_pw || !user_name) {
            alert('현재 비밀번호와 이름은 필수 입력 항목입니다.');
            return;
        }

        const newPassword = editPasswordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        if (newPassword && newPassword !== confirmPassword) {
            alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            return;
        }

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'Update';

        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'id';
        idInput.value = user_id;

        const pwInput = document.createElement('input');
        pwInput.type = 'hidden';
        pwInput.name = 'pw';
        pwInput.value = user_pw;
        
        const nameInput = document.createElement('input');
        nameInput.type = 'hidden';
        nameInput.name = 'name';
        nameInput.value = user_name;

        if (newPassword) {
            const newPwInput = document.createElement('input');
            newPwInput.type = 'hidden';
            newPwInput.name = 'newPw';
            newPwInput.value = newPassword;
            form.appendChild(newPwInput);
        }

        form.appendChild(idInput);
        form.appendChild(pwInput);
        form.appendChild(nameInput);
        
        document.body.appendChild(form);
        form.submit();
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