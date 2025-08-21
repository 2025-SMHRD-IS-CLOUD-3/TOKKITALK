// URL 파라미터 가져오기 함수 (기존 유지)
function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// 헤더 UI 업데이트 (기존 유지)
function updateHeaderUI(isLoggedIn, name = '') {
    const authButtons = document.getElementById('auth-buttons');
    const userInfoArea = document.getElementById('user-info-area');
    const headerUserName = document.getElementById('headerUserName');
    const welcomeMessage = document.querySelector('#user-info-area .welcome-text');

    if (isLoggedIn) {
        if (authButtons) authButtons.style.display = 'none';
        if (userInfoArea) userInfoArea.style.display = 'flex';
        if (headerUserName) headerUserName.textContent = name;
        if (welcomeMessage) welcomeMessage.textContent = `${name}님 환영합니다!`;
    } else {
        if (authButtons) authButtons.style.display = 'flex';
        if (userInfoArea) userInfoArea.style.display = 'none';
    }
}

// ⭐ DB 통신을 가정한 API 함수 (새로 추가) ⭐
const api = {
    // 모든 대화 기록을 DB에서 가져오는 함수
    // 실제로는 fetch()나 axios 등을 사용해 서버에 GET 요청을 보내야 함
    fetchHistory: async () => {
        // 백엔드에서 JSON 데이터를 반환한다고 가정
        // 예시: const response = await fetch('/api/history');
        // 예시: return await response.json();
        
        // 임시 데이터 반환 (실제 DB 연결 시 이 부분 삭제)
        return [
            {
                datetime: "2025년 8월 21일 15:26:43",
                inputText: "너 요즘 표정이 왜 그래?",
                imageData: null,
                result: {
                    surface_meaning: "상대방의 현재 감정이나 상태에 대해 질문하고 있습니다.",
                    hidden_meaning: "걱정하는 마음을 표현하고, 힘든 일이 있는지 알고 싶어 합니다.",
                    emotion: "걱정, 염려",
                    advice: {
                        positive: "완전 예뻐! 색깔이 너한테 잘 어울려 ✨ 어디서 샀어?",
                        empathetic: "많이 힘들었구나, 고생했어 😔 내가 옆에서 쉬게 해줄게",
                        witty: "위험한 질문이네 🤔 난 이거 한 표! 기분에 맞춰 정하자!",
                        actionable: "내가 맛있는 거라도 사줄까? 🍕 오늘은 특별히 예쁘니까"
                    }
                }
            },
        ];
    },

    // 특정 기록을 DB에서 삭제하는 함수 (DELETE 요청)
    deleteEntry: async (entryId) => {
        // 예시: const response = await fetch(`/api/history/${entryId}`, { method: 'DELETE' });
        // 예시: return response.ok;
        
        // 성공 여부 임시 반환
        return true;
    }
};

// 페이지 로드 후 실행될 이벤트 리스너
document.addEventListener('DOMContentLoaded', async function() {
    const useridFromUrl = getUrlParameter('userid');

    if (useridFromUrl) {
        sessionStorage.setItem('loggedIn', 'true');
        sessionStorage.setItem('userName', useridFromUrl);
    }

    const loggedInStatus = sessionStorage.getItem('loggedIn') === 'true';
    const storedUserName = sessionStorage.getItem('userName');

    updateHeaderUI(loggedInStatus, storedUserName);

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');
            alert("로그아웃되었습니다.");
            window.location.href = "main.html";
        });
    }

    // ⭐ DB에서 데이터 가져와서 화면 렌더링 ⭐
    await renderHistory();
});

// 히스토리 렌더링 함수
const historyContainer = document.getElementById('historyContainer');

async function renderHistory() {
    if (!historyContainer) return;
    
    historyContainer.innerHTML = '<h1>TOKKI TALK 대화 기록</h1>'; 
    
    try {
        const historyData = await api.fetchHistory(); // ⭐ DB에서 데이터 가져오기
        
        historyData.forEach((entry, index) => {
            const entryDiv = document.createElement('div');
            entryDiv.className = 'entry';
            
            const imageData = entry.imageData ? `<img src="${entry.imageData}" alt="업로드 이미지" style="max-width: 200px;">` : "";
            
            const adviceHTML = `
                <div class="advice-header">
                    <span class="icon">⏰</span>
                    <strong class="advice-title">TOKKI TALK의 제안들</strong>
                </div>
                ${entry.result.advice.positive ? `<div class="advice-item"><strong>적극대응형:</strong><span>${entry.result.advice.positive}</span></div>` : ''}
                ${entry.result.advice.empathetic ? `<div class="advice-item"><strong>공감근접형:</strong><span>${entry.result.advice.empathetic}</span></div>` : ''}
                ${entry.result.advice.witty ? `<div class="advice-item"><strong>재치있는 응답형:</strong><span>${entry.result.advice.witty}</span></div>` : ''}
                ${entry.result.advice.actionable ? `<div class="advice-item"><strong>구체적행동형:</strong><span>${entry.result.advice.actionable}</span></div>` : ''}
            `;
            
            entryDiv.innerHTML = `
                <h3>${entry.datetime}</h3>
                <p><strong>입력 메시지:</strong> ${entry.inputText || "(이미지 입력)"}</p>
                ${imageData}
                <p><strong>표면적 의미:</strong> ${entry.result.surface_meaning}</p>
                <p><strong>숨은 의도:</strong> ${entry.result.hidden_meaning}</p>
                <p><strong>감정 상태:</strong> ${entry.result.emotion}</p>
                
                <div class="advice-box">
                    ${adviceHTML}
                </div>
                <button class="delete-button" onclick="deleteHistory(${index})">🗑️ 삭제</button>
            `;
            
            historyContainer.appendChild(entryDiv);
        });
    } catch (error) {
        console.error("Failed to fetch history data:", error);
        historyContainer.innerHTML = '<p>데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.</p>';
    }
}

async function deleteHistory(index) {
    if (confirm("정말로 이 기록을 삭제하시겠습니까?")) {
        // ⭐ DB 삭제 로직
        const entryToDelete = await api.fetchHistory();
        const success = await api.deleteEntry(entryToDelete[index].id);
        
        if (success) {
            alert("기록이 성공적으로 삭제되었습니다.");
            await renderHistory(); // 삭제 후 목록 다시 불러오기
        } else {
            alert("삭제에 실패했습니다.");
        }
    }
}