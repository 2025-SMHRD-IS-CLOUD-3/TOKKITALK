const quizData = [
    {
		question: "Q1. 다음 사진을 보고 당신의 선택을 골라주세요.",
		                image: src="Image/센스테스트 문제1.png",
		                options: [
		                    "A. 제발 갖다 버리고 그만 좀 입어.",
		                    "B. 옷 진짜 잘 어울린다.",
		                    "C. 옷이 이거밖에 없어?."
		                ],
		                correctAnswer: "A. 제발 갖다 버리고 그만 좀 입어."
    },
    {
		question: "Q2. 다음 상황에 대한 적절한 행동으로 올바른 것은?",
		                image: src="Image/센스테스트 문제2.png",
		                options: [
		                    "A. 무슨 일 있었어? 대화를 시도한다.",
		                    "B. 질 수 없지 같이 화를 낸다.",
		                    "C. 조용히 음식을 대령한다."
		                ],
		                correctAnswer: "C. 조용히 음식을 대령한다."
    },
    {
		question: "Q3. 세 번째 문제의 질문입니다.",
		                image: src="Image/센스테스트 문제3.png",
		                options: [
		                    "A. 배 안 고픈데.",
		                    "B. 정말 아무거나.",
		                    "C. 옵션을 주면 고를 테니 말해봐."
		                ],
		                correctAnswer: "C. 옵션을 주면 고를 테니 말해봐."
    },
    {
		question: "Q4. 네 번째 문제의 질문입니다.",
		                image: src="Image/센스테스트 문제4.png",
		                options: [
		                    "A. 안 쪘어, 그리고 좀 찌면 어때? 쪄도 너무예뻐",
		                    "B. 뭐가 살쪄!!!!",
		                    "C. 나도 쪘어 행복 하나봐."
		                ],
		                correctAnswer: "A. 안 쪘어, 그리고 좀 찌면 어때? 쪄도 너무예뻐"
    },
    {
		question: "Q5. 마지막 문제의 질문입니다.",
		                image: src="Image/센스테스트 문제5.png",
		                options: [
		                    "A. 나는 푸는게 좋아.",
		                    "B. 나는 묶는게 좋아.",
		                    "C. 풀면 성숙해 보이고 묶으면 귀여워."
		                ],
		                correctAnswer: "C. 풀면 성숙해 보이고 묶으면 귀여워."
    }
];

let currentQuestionIndex = 0;
let score = 0;

const quizContainer = document.querySelector('.quiz-container');
const questionTitle = document.querySelector('.question-title');
const chatImage = document.querySelector('.chat-image');
const answerOptionsContainer = document.querySelector('.answer-options');
const headerUserName = document.getElementById('headerUserName');
const welcomeText = document.getElementById('welcomeText');
const logoutBtn = document.getElementById('logoutBtn');

function loadQuiz() {
    if (currentQuestionIndex < quizData.length) {
        const currentQuiz = quizData[currentQuestionIndex];
        questionTitle.textContent = currentQuiz.question;
        chatImage.src = currentQuiz.image;
        answerOptionsContainer.innerHTML = '';

        currentQuiz.options.forEach(option => {
            const optionElement = document.createElement('div');
            optionElement.classList.add('answer-option');
            optionElement.textContent = option;
            optionElement.addEventListener('click', () => selectAnswer(option));
            answerOptionsContainer.appendChild(optionElement);
        });
    } else {
        showResultPage();
    }
}

function selectAnswer(selectedOption) {
    const currentQuiz = quizData[currentQuestionIndex];
    if (selectedOption === currentQuiz.correctAnswer) {
        score++;
    }
    
    currentQuestionIndex++;
    loadQuiz();
}

function showResultPage() {
    quizContainer.innerHTML = '';

    const resultCard = document.createElement('div');
    resultCard.classList.add('result-card');
    
    const resultTitle = document.createElement('h2');
    resultTitle.classList.add('result-title');
    
    const resultText = document.createElement('p');
    resultText.classList.add('result-text');

    let resultMessage = '';
	if (score === 5) {
	        resultMessage = "당신은 센스 만점! 모든 질문에 완벽하게 대처하셨네요. 👍";
	        imageSrc = 'Image/1.jpg';
	    } else if (score >= 3) {
	        resultMessage = "꽤 괜찮은 센스! 조금만 더 노력하면 완벽한 센스쟁이가 될 거예요. 😉";
	        imageSrc = 'Image/2.jpg';
	    } else {
	        resultMessage = "아직 센스가 부족하네요. 다음번엔 더 좋은 결과를 기대해 봐요! 🤔";
	        imageSrc = 'Image/3.jpg';
	    }

    resultTitle.textContent = `센스고사 결과: ${score} / 5점`;
    resultText.textContent = resultMessage;
    
    resultCard.appendChild(resultTitle);
    resultCard.appendChild(resultText);

    quizContainer.appendChild(resultCard);
}

// 로그인 상태 및 사용자 이름 동적 로드
document.addEventListener('DOMContentLoaded', () => {
    const loggedIn = sessionStorage.getItem('loggedIn');
    const userName = sessionStorage.getItem('userName');
    
    if (loggedIn === 'true' && userName) {
        // 헤더에 사용자 이름 표시
        headerUserName.textContent = userName;
        
        // 로그아웃 버튼에 이벤트 리스너 추가
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userName');
            alert("로그아웃되었습니다.");
            window.location.href = "main.html";
        });
    } else {
        // 로그인 상태가 아닐 경우, 로그인/회원가입 버튼을 표시
        document.getElementById('auth-buttons').style.display = 'flex';
        document.getElementById('user-info-area').style.display = 'none';
        
        // 마이페이지 같은 페이지는 로그인 필수 로직 추가
        if (window.location.pathname.includes('SenseTest.html')) {
            alert('로그인이 필요한 서비스입니다.');
            window.location.href = "main.html";
        }
    }
    
    loadQuiz();
});