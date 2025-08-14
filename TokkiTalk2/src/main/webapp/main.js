/**
 * 
 */

// 홈으로 이동 함수
function goHome() {
    window.location.reload(); // 현재 페이지를 새로고침하여 홈으로 이동
    // 또는 특정 URL로 이동하려면: window.location.href = '/';
}

// 로그인 모달 열기
function openLoginModal() {
    document.getElementById('loginModal').style.display = 'flex';
}

// 로그인 모달 닫기
function closeLoginModal() {
    document.getElementById('loginModal').style.display = 'none';
}

// 회원가입 모달 열기
function openSignupModal() {
    document.getElementById('signupModal').style.display = 'flex';
}

// 회원가입 모달 닫기
function closeSignupModal() {
    document.getElementById('signupModal').style.display = 'none';
}

// 성별 선택 함수
function selectGender(element) {
    // 1. 모든 .gender-radio 요소를 찾아서 'checked' 클래스를 제거합니다.
    const radios = document.querySelectorAll('.gender-radio');
    radios.forEach(radio => {
        radio.classList.remove('checked');
    });

    // 2. 함수로 전달받은 'element' (클릭된 라디오 버튼)에 'checked' 클래스를 추가합니다.
    element.classList.add('checked');
}

// DOM이 로드된 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 모달 외부 클릭시 닫기
    document.getElementById('loginModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeLoginModal();
        }
    });
    
    document.getElementById('signupModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeSignupModal();
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeLoginModal();
            closeSignupModal();
        }
    });

    // 버튼 클릭 이벤트 (모든 CTA 버튼에 대해)
    document.querySelectorAll('.cta-button').forEach(button => {
        button.addEventListener('click', function() {
            const buttonText = this.textContent.trim();
            if (buttonText.includes('대화해볼래')) {
                alert('대화를 시작합니다! 💬');
            } else if (buttonText.includes('센스고사')) {
                alert('센스고사를 시작합니다! 🎉');
            }
        });
    });

    // 네비게이션 버튼 이벤트
    document.querySelector('.btn-login').addEventListener('click', function() {
        openLoginModal();
    });

    document.querySelector('.btn-signup').addEventListener('click', function() {
        openSignupModal();
    });
    
    // 캐릭터 아이콘 클릭 이벤트
    document.querySelectorAll('.character-icon').forEach(icon => {
        icon.addEventListener('click', function() {
            // 선택된 아이콘 효과 (선택적)
            document.querySelectorAll('.character-icon').forEach(i => i.style.border = 'none');
            this.style.border = '3px solid #333';
        });
    });
    
    // 로그인 모달에서 회원가입 버튼 클릭시
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