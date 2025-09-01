const authButtons = document.getElementById('auth-buttons');
const userInfoArea = document.getElementById('user-info-area');
const headerUserName = document.getElementById('headerUserName');
const logoutBtn = document.getElementById('logoutBtn');
const historyContainer = document.getElementById('historyContainer');
const loading = document.getElementById('loading');
const noHistory = document.getElementById('noHistory');
const paginationContainer = document.getElementById('pagination'); // 페이지네이션 컨테이너 변수 추가

const ITEMS_PER_PAGE = 5; // 페이지당 항목 수
let totalConversations = []; // 전체 대화 기록을 저장할 배열

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

function getApiBase() {
	const host = window.location.hostname;
	if (host === '127.0.0.1' || host === 'localhost') return 'http://localhost:8081/TokkiTalk2';
	return window.location.origin;
}

async function loadHistory() {
	try {
		const res = await fetch(getApiBase() + '/getHistory');
		if (!res.ok) throw new Error('서버 응답 실패: ' + res.status);
		const historyItems = await res.json();

		loading.style.display = 'none';
		if (!historyItems || historyItems.length === 0) {
			noHistory.style.display = 'block';
			return;
		}

		// 전체 대화 기록을 변수에 저장하고 페이지를 그룹화
		totalConversations = groupByConversation(historyItems);

		// 첫 번째 페이지 표시
		displayHistory(1);
	} catch (e) {
		console.error('히스토리 로드 오류:', e);
		showError('히스토리를 불러오는데 실패했습니다.');
	}
}

function displayHistory(page) {
	historyContainer.innerHTML = '';
	paginationContainer.innerHTML = ''; // 페이지네이션 초기화

	const startIndex = (page - 1) * ITEMS_PER_PAGE;
	const endIndex = startIndex + ITEMS_PER_PAGE;
	const paginatedConversations = totalConversations.slice(startIndex, endIndex);

	paginatedConversations.forEach(conversation => {
		const conversationDiv = document.createElement('div');
		conversationDiv.className = 'conversation-item';
		const conversationId = 'conv-' + conversation.chatIds.join('-');
		conversationDiv.id = conversationId;

		const conversationContentHtml = conversation.messages.map(msg => {
			let roleClass = '', roleName = '';
			if (msg.role === 'user') { roleClass = 'user'; roleName = '사용자'; }
			else if (msg.role === 'assistant') { roleClass = 'assistant'; roleName = 'TOKKITALK'; }

			let messageBody = msg.role === 'assistant' ? formatTokkiTalk(msg.content) : `<pre>${msg.content || ''}</pre>`;
			if (msg.role === 'user' && msg.imageBase64) {
				messageBody += `<img src="data:image/png;base64,${msg.imageBase64}" style="max-width:300px;height:auto;margin-top:10px;border-radius:10px;box-shadow:0 4px 8px rgba(0,0,0,0.2);">`;
			}

			return `<div class="message-item ${roleClass}"><div class="message-header"><span class="message-role">${roleName}</span></div><div class="message-content">${messageBody}</div></div>`;
		}).join('');

		conversationDiv.innerHTML = `
		    <div class="conversation-header" onclick="toggleConversation(this)">
		        <div class="conversation-info">
		            <span class="conversation-date">${conversation.date}</span>
		            <span class="conversation-time">${conversation.time}</span>
		        </div>
		        <button class="btn-toggle">
		            <span class="toggle-icon">▶</span>
		        </button>
		    </div>
		    <div class="conversation-content">
		        ${conversationContentHtml}
		        <div style="text-align:right;margin-top:15px;">
		            <button onclick="deleteConversation('${conversationId}', [${conversation.chatIds}])" class="btn-delete-history">
		                🗑️ 히스토리 삭제
		            </button>
		        </div>
		    </div>
		`;
		
		historyContainer.appendChild(conversationDiv);
	});

	// 페이지네이션 UI 생성
	createPagination(page);
}

async function deleteConversation(conversationElementId, chatIds) {
  if (!confirm("정말로 이 대화 기록을 삭제하시겠습니까?")) return;
  try {
    // chatIds를 문자열이 아닌 배열로 처리하도록 수정
    const ids = Array.isArray(chatIds) ? chatIds : [chatIds];

    const response = await fetch(getApiBase() + '/deleteHistory', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ chatIds: ids })
    });

    if (!response.ok) {
      const errorResult = await response.json().catch(() => ({ error: '서버 응답을 처리할 수 없습니다.' }));
      throw new Error(errorResult.error || '서버에서 오류');
    }
    const result = await response.json();
    if (result.success) {
      const el = document.getElementById(conversationElementId);
      if (el) {
        el.style.transition = 'opacity 0.5s ease';
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 500);
      }
      // 삭제 후 히스토리 다시 로드하여 페이지네이션 업데이트
      loadHistory();
    } else {
      throw new Error(result.error || '삭제 실패');
    }
  } catch (e) {
    alert('삭제 오류: ' + e.message);
  }
}

function groupByConversation(historyItems) {
  if (!historyItems || historyItems.length === 0) return [];

  const conversations = [];
  const sortedItems = historyItems.sort((a, b) => {
    const dateA = new Date(a.created_at), dateB = new Date(b.created_at);
    return dateA - dateB || a.chat_id - b.chat_id;
  });

  let currentMessages = [];
  
  // 날짜와 시간을 로컬 시간대로 변환하여 대화를 저장하는 헬퍼 함수
  const saveConversation = (messages) => {
    if (messages.length === 0) return;
    const last = messages[messages.length - 1];
    const lastTime = new Date(last.created_at);
    
    // 로컬 시간 기준으로 날짜 포맷팅
    const date = `${lastTime.getFullYear()}-${(lastTime.getMonth() + 1).toString().padStart(2, '0')}-${lastTime.getDate().toString().padStart(2, '0')}`;
    // 로컬 시간 기준으로 시간 포맷팅
    const time = `${lastTime.getHours().toString().padStart(2, '0')}:${lastTime.getMinutes().toString().padStart(2, '0')}:${lastTime.getSeconds().toString().padStart(2, '0')}`;

    conversations.push({
      date: date,
      time: time,
      messages: messages,
      chatIds: messages.map(m => m.id)
    });
  };

  sortedItems.forEach(item => {
    currentMessages.push({
      id: item.chat_id,
      role: item.role,
      content: item.message_text,
      imageBase64: item.image_base64 || null,
      created_at: item.created_at
    });

    if (item.role === 'assistant') {
      saveConversation(currentMessages);
      currentMessages = [];
    }
  });

  // 마지막 대화가 TOKKITALK 응답으로 끝나지 않았을 경우 저장
  saveConversation(currentMessages);
  
  return conversations.reverse();
}

function toggleConversation(header) {
	const content = header.nextElementSibling;
	const icon = header.querySelector('.toggle-icon');
	if (!content.style.display || content.style.display === 'none') {
		content.style.display = 'flex';
		icon.textContent = '▼';
	} else {
		content.style.display = 'none';
		icon.textContent = '▶';
	}
}

function showError(msg) {
	loading.style.display = 'none';
	historyContainer.innerHTML = `<div style="text-align:center;padding:50px;"><div style="font-size:48px;">⚠️</div><h3>오류가 발생했습니다</h3><p>${msg}</p></div>`;
}

function formatTokkiTalk(raw) {
	let data;
	try {
		data = (typeof raw === 'object' && raw) ? raw : JSON.parse(raw);
	} catch (e) {
		return `<pre>${raw}</pre>`;
	}
	if (!data || !data.advice || !data.surface_meaning || !data.hidden_meaning || !data.emotion) {
		return `<pre>${JSON.stringify(data, null, 2)}</pre>`;
	}
	const adviceHtml = data.advice.map(a => `<div style="margin-top:5px;"><strong>- ${a.style}:</strong> <span>${a.text}</span></div>`).join('');
	return `<div><p><strong>📝 표면적 의미:</strong><br>${data.surface_meaning.one_line}</p><p style="margin-top:10px;"><strong>🔍 숨은 의도:</strong><br>${data.hidden_meaning.one_line}</p><p style="margin-top:10px;"><strong>😊 감정 상태:</strong><br>${data.emotion.label}</p><div style="margin-top:15px;padding:10px;background:#fafafa;border-radius:8px;"><strong>💡 TOKKI의 제안들</strong>${adviceHtml}</div></div>`;
}

// 페이지네이션 UI를 생성하는 함수
function createPagination(currentPage) {
	const totalPages = Math.ceil(totalConversations.length / ITEMS_PER_PAGE);
	if (totalPages <= 1) return;

	// 이전 버튼
	const prevBtn = document.createElement('button');
	prevBtn.className = 'pagination-btn';
	prevBtn.innerHTML = '&lt;';
	if (currentPage === 1) {
		prevBtn.classList.add('disabled');
		prevBtn.disabled = true;
	} else {
		prevBtn.onclick = () => displayHistory(currentPage - 1);
	}
	paginationContainer.appendChild(prevBtn);

	// 페이지 번호
	const pagesToShow = getVisiblePages(currentPage, totalPages);
	pagesToShow.forEach(page => {
		if (page === '...') {
			const ellipsis = document.createElement('span');
			ellipsis.className = 'pagination-ellipsis';
			ellipsis.textContent = '...';
			paginationContainer.appendChild(ellipsis);
		} else {
			const pageBtn = document.createElement('button');
			pageBtn.className = 'pagination-btn';
			pageBtn.textContent = page;
			if (page === currentPage) pageBtn.classList.add('active');
			pageBtn.onclick = () => displayHistory(page);
			paginationContainer.appendChild(pageBtn);
		}
	});

	// 다음 버튼
	const nextBtn = document.createElement('button');
	nextBtn.className = 'pagination-btn';
	nextBtn.innerHTML = '&gt;';
	if (currentPage === totalPages) {
		nextBtn.classList.add('disabled');
		nextBtn.disabled = true;
	} else {
		nextBtn.onclick = () => displayHistory(currentPage + 1);
	}
	paginationContainer.appendChild(nextBtn);
}

// 이미지와 같은 페이지 번호 그룹을 계산하는 함수
function getVisiblePages(currentPage, totalPages) {
    const pages = [];
    const maxVisible = 5; // 화면에 표시될 페이지 번호 최대 개수 (양 끝 페이지 제외)
    let startPage, endPage;

    // 전체 페이지가 7개 이하일 경우 (줄임표 필요 없음)
    if (totalPages <= maxVisible + 2) { 
        for (let i = 1; i <= totalPages; i++) {
            pages.push(i);
        }
        return pages;
    }

    // 현재 페이지를 중심으로 5개 페이지 번호 계산
    let centerStart = currentPage - Math.floor(maxVisible / 2);
    let centerEnd = currentPage + Math.floor(maxVisible / 2);

    // 시작 부분이 1보다 작아지면 조정
    if (centerStart < 1) {
        centerStart = 1;
        centerEnd = maxVisible;
    }

    // 끝 부분이 전체 페이지보다 커지면 조정
    if (centerEnd > totalPages) {
        centerEnd = totalPages;
        centerStart = totalPages - maxVisible + 1;
    }
    
    // 계산된 범위의 페이지 번호를 배열에 추가
    for (let i = centerStart; i <= centerEnd; i++) {
        pages.push(i);
    }

    // 첫 페이지 (1)과 줄임표 추가
    if (pages[0] > 1) {
        if (pages[0] > 2) {
            pages.unshift('...');
        }
        pages.unshift(1);
    }

    // 마지막 페이지와 줄임표 추가
    if (pages[pages.length - 1] < totalPages) {
        if (pages[pages.length - 1] < totalPages - 1) {
            pages.push('...');
        }
        pages.push(totalPages);
    }

    return pages;
}

document.addEventListener('DOMContentLoaded', () => {
	updateHeaderUI();
	loadHistory();
	if (logoutBtn) {
		logoutBtn.addEventListener('click', () => {
			sessionStorage.removeItem('loggedIn');
			sessionStorage.removeItem('userName');
			alert('로그아웃되었습니다.');
			window.location.href = 'main.html';
		});
	}
});