// 가상의 DB 데이터 (더 이상 '김토끼' 정보가 하드코딩되지 않음)
const mockDatabase = {
    userProfile: {
        name: "",
        email: "",
        password: ""
    },
    chatList: [],
    chatDetails: {}
};

// 동적으로 데이터를 로딩하는 함수 (가상의 대화 내용은 유지)
function loadChatDetails(chatId) {
    return new Promise(resolve => {
        setTimeout(() => {
            const details = {
                1: {
                    title: "그녀와 나눈 대화 #1",
                    messages: [
                        { type: "received", text: "안녕! 요즘 어떻게 지내?", time: "오후 2:30" },
                        { type: "sent", text: "덕분에 잘 지내지~ 오랜만에 연락했네!", time: "오후 2:31" },
                        { type: "received", text: "그러게, 바빴지? 조만간 시간 되면 밥 한 번 먹자!", time: "오후 2:33" },
                        { type: "sent", text: "좋아! 언제쯤 괜찮아?", time: "오후 2:35" },
                        { type: "received", text: "이번 주말 어때?", time: "오후 2:38" }
                    ]
                },
                2: {
                    title: "그녀와 나눈 대화 #2",
                    messages: [
                        { type: "sent", text: "오늘 날씨 정말 좋다! 뭐해?", time: "오전 10:00" },
                        { type: "received", text: "응, 정말 좋네! 카페에서 책 읽고 있어.", time: "오전 10:05" },
                        { type: "sent", text: "오, 여유롭고 좋겠다. 나도 곧 갈게!", time: "오전 10:08" }
                    ]
                },
                3: {
                    title: "그녀와 나눈 대화 #3",
                    messages: [
                        { type: "received", text: "이거 완전 내 얘기 아니냐ㅋㅋ", time: "오후 8:15" },
                        { type: "sent", text: "ㅋㅋㅋㅋ 진짜 빵 터졌네!", time: "오후 8:17" }
                    ]
                }
            };
            mockDatabase.chatDetails[chatId] = details[chatId];
            resolve(details[chatId]);
        }, 500);
    });
}

const chatListElement = document.getElementById('chatList');
const userNameElement = document.getElementById('userName');
const userEmailElement = document.getElementById('userEmail');
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
    setTimeout(() => {
        const chats = mockDatabase.chatList;
        renderChatList(chats);
    }, 1000);
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

async function fetchChatDetails(chatId) {
    modalContentArea.innerHTML = '<div class="loading-message">대화 내용을 불러오는 중...</div>';
    chatModal.style.display = "flex";

    const chat = mockDatabase.chatDetails[chatId];
    if (chat) {
        showChatModal(chat);
    } else {
        try {
            const loadedChat = await loadChatDetails(chatId);
            showChatModal(loadedChat);
        } catch (error) {
            console.error('Failed to load chat details:', error);
            modalContentArea.innerHTML = '<div class="loading-message">대화 내용을 불러올 수 없습니다.</div>';
        }
    }
}

function showChatModal(chat) {
    modalContentArea.innerHTML = '';
    
    const titleElement = document.createElement('h2');
    titleElement.classList.add('chat-title');
    titleElement.textContent = chat.title;
    modalContentArea.appendChild(titleElement);
    
    const messagesContainer = document.createElement('div');
    messagesContainer.classList.add('chat-messages');
    modalContentArea.appendChild(messagesContainer);

    chat.messages.forEach(msg => {
        const messageRow = document.createElement('div');
        messageRow.classList.add('message-row', msg.type);

        const timestamp = document.createElement('div');
        timestamp.classList.add('message-timestamp');
        timestamp.textContent = msg.time;

        const bubble = document.createElement('div');
        bubble.classList.add('message-bubble');
        bubble.textContent = msg.text;

        if (msg.type === 'sent') {
            messageRow.appendChild(timestamp);
            messageRow.appendChild(bubble);
        } else {
            messageRow.appendChild(bubble);
            messageRow.appendChild(timestamp);
        }
        messagesContainer.appendChild(messageRow);
    });
}

function updateProfileDisplay(profile) {
    userNameElement.textContent = `${profile.name}님`;
    userEmailElement.textContent = profile.email;
    headerUserName.textContent = profile.name;
}

if (openProfileModalBtn) {
    openProfileModalBtn.addEventListener('click', function() {
        editNameInput.value = mockDatabase.userProfile.name;
        currentPasswordInput.value = "";
        editPasswordInput.value = "";
        confirmPasswordInput.value = "";
        profileModal.style.display = "flex";
    });
}

if (saveProfileBtn) {
    saveProfileBtn.addEventListener('click', function() {
        const newName = editNameInput.value.trim();
        const currentPassword = currentPasswordInput.value.trim();
        const newPassword = editPasswordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        if (newPassword === '' && newName !== mockDatabase.userProfile.name) {
            mockDatabase.userProfile.name = newName;
            updateProfileDisplay(mockDatabase.userProfile);
            sessionStorage.setItem('userName', newName);
            profileModal.style.display = "none";
            alert('이름이 성공적으로 변경되었습니다!');
            return;
        }
        
        if (newPassword !== '') {
            if (currentPassword === '') {
                alert('비밀번호를 변경하려면 현재 비밀번호를 입력해주세요.');
                return;
            }

            if (currentPassword !== mockDatabase.userProfile.password) {
                alert('현재 비밀번호가 일치하지 않습니다.');
                return;
            }

            if (newPassword !== confirmPassword) {
                alert('새 비밀번호가 일치하지 않습니다.');
                return;
            }
            
            mockDatabase.userProfile.password = newPassword;
            alert('비밀번호가 성공적으로 변경되었습니다!');
        }
        
        if (newName !== mockDatabase.userProfile.name) {
            mockDatabase.userProfile.name = newName;
            sessionStorage.setItem('userName', newName);
        }

        updateProfileDisplay(mockDatabase.userProfile);
        profileModal.style.display = "none";
        alert('프로필이 성공적으로 저장되었습니다!');
    });
}

if (deleteAccountBtn) {
    deleteAccountBtn.addEventListener('click', function() {
        if (confirm("정말 회원 탈퇴하시겠습니까? 모든 정보가 삭제됩니다.")) {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');

            mockDatabase.userProfile = { name: "", email: "", password: "" };
            mockDatabase.chatList = [];
            mockDatabase.chatDetails = {};

            alert('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
            window.location.href = 'main.html';
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

    if (isLoggedIn && storedUserName) {
        mockDatabase.userProfile.name = storedUserName;
        mockDatabase.userProfile.email = `${storedUserName.toLowerCase().replace(/\s/g, '')}@email.com`;
        mockDatabase.userProfile.password = "1234"; 
        
        // 특정 사용자에게만 가상의 대화 기록을 제공
        if (storedUserName === "김토끼") {
            mockDatabase.chatList = [
                { id: 1, title: "그녀와 나눈 대화 #1", date: "2024-08-13" },
                { id: 2, title: "그녀와 나눈 대화 #2", date: "2024-08-11" },
                { id: 3, title: "그녀와 나눈 대화 #3", date: "2024-08-09" }
            ];
        } else {
            mockDatabase.chatList = [];
        }
        
    } else {
        mockDatabase.userProfile = {
            name: "방문자",
            email: "guest@email.com",
            password: ""
        };
        mockDatabase.chatList = [];
    }

    updateHeaderUI(isLoggedIn, mockDatabase.userProfile.name);
    updateProfileDisplay(mockDatabase.userProfile);
    fetchChatList();

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
});