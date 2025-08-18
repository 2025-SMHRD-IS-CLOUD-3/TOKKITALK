document.addEventListener('DOMContentLoaded', function() {

    // 홈으로 이동
    function goHome() {
        window.location.href = 'main.html';
    }
    window.goHome = goHome;

    // 로그인 모달 열기/닫기
    function openLoginModal() {
        document.getElementById('loginModal').style.display = 'flex';
    }
    function closeLoginModal() {
        document.getElementById('loginModal').style.display = 'none';
    }
    window.openLoginModal = openLoginModal;
    window.closeLoginModal = closeLoginModal;

    // 회원가입 모달 열기/닫기
    function openSignupModal() {
        document.getElementById('signupModal').style.display = 'flex';
    }
    function closeSignupModal() {
        document.getElementById('signupModal').style.display = 'none';
    }
    window.openSignupModal = openSignupModal;
    window.closeSignupModal = closeSignupModal;

    // 성별 선택
    function selectGender(gender, element) {
        const radios = document.querySelectorAll('.gender-radio');
        radios.forEach(radio => radio.classList.remove('checked'));
        element.classList.add('checked');
    }
    window.selectGender = selectGender;

    // 모달 외부 클릭 시 닫기
    document.getElementById('loginModal').addEventListener('click', function(e) {
        if (e.target === this) closeLoginModal();
    });
    document.getElementById('signupModal').addEventListener('click', function(e) {
        if (e.target === this) closeSignupModal();
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeLoginModal();
            closeSignupModal();
        }
    });

    // 버튼 이벤트
    document.querySelector('.btn-login').addEventListener('click', openLoginModal);
    document.querySelector('.btn-signup').addEventListener('click', openSignupModal);

    // 캐릭터 아이콘 클릭
    document.querySelectorAll('.character-icon').forEach(icon => {
        icon.addEventListener('click', function() {
            document.querySelectorAll('.character-icon').forEach(i => i.style.border = 'none');
            this.style.border = '3px solid #333';
        });
    });

    // 로그인 모달 → 회원가입 모달 전환
    document.querySelector('.modal-signup-btn').addEventListener('click', function() {
        closeLoginModal();
        openSignupModal();
    });

    // 중복 확인 버튼
    document.querySelector('.duplicate-check-btn').addEventListener('click', function() {
        alert('중복 확인을 진행합니다.');
    });

    // 회원가입 완료 버튼
    document.querySelector('.signup-confirm-btn').addEventListener('click', function() {
        alert('회원가입이 완료되었습니다! 🎉');
        closeSignupModal();
    });
});
