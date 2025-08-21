// DOM 요소 가져오기
const authButtons = document.getElementById('auth-buttons');
const userInfoArea = document.getElementById('user-info-area');
const headerUserName = document.getElementById('headerUserName');
const logoutBtn = document.getElementById('logoutBtn');




// 로그인 상태에 따라 헤더 UI를 업데이트하는 함수
function updateHeaderUI() {
    const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true';
    const userName = sessionStorage.getItem('userName');

    if (isLoggedIn && userName) {
        authButtons.style.display = 'none';
        userInfoArea.style.display = 'flex';
        headerUserName.textContent = userName;
    } else {
        authButtons.style.display = 'flex';
        userInfoArea.style.display = 'none';
    }
}

// 네비게이션 및 버튼 이벤트 리스너
document.addEventListener('DOMContentLoaded', () => {
    updateHeaderUI();

    // 로그아웃 버튼 이벤트
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');
            alert('로그아웃되었습니다.');
            window.location.href = 'main.html';
        });
    }

    // 네비게이션 링크 클릭 이벤트 (로그인 상태에 따라 이동 제어)
    document.querySelectorAll('.nav-item').forEach(link => {
        link.addEventListener('click', (e) => {
            const href = e.target.getAttribute('href');
            if (href === 'intro.html' || href === '/권동환/K메인페이지.html') {
                return; // 소개, 메인 페이지는 로그인 없이 이동 가능
            } else {
                const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true';
                if (!isLoggedIn) {
                    e.preventDefault();
                    alert('로그인이 필요한 서비스입니다.');
                    window.location.href = 'main.html';
                }
            }
        });
    });
});


