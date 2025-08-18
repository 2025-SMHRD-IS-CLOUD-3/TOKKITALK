const quizData = [
    {
        question: "Q1. 다음 카톡 이후 적절한 행동으로 올바른 것은?",
        image: "https://i.imgur.com/your_chat_image_1.png",
        options: [
            "A. 선택지 1 내용입니다.",
            "B. 선택지 2 내용입니다.",
            "C. 선택지 3 내용입니다."
        ],
        correctAnswer: "A. 선택지 1 내용입니다."
    },
    {
        question: "Q2. 다른 상황에 대한 적절한 행동으로 올바른 것은?",
        image: "https://i.imgur.com/your_chat_image_2.png",
        options: [
            "A. 다음 선택지 1 내용입니다.",
            "B. 다음 선택지 2 내용입니다.",
            "C. 다음 선택지 3 내용입니다."
        ],
        correctAnswer: "B. 다음 선택지 2 내용입니다."
    },
    {
        question: "Q3. 세 번째 문제의 질문입니다.",
        image: "https://i.imgur.com/your_chat_image_3.png",
        options: [
            "A. 세 번째 선택지 1 내용입니다.",
            "B. 세 번째 선택지 2 내용입니다.",
            "C. 세 번째 선택지 3 내용입니다."
        ],
        correctAnswer: "C. 세 번째 선택지 3 내용입니다."
    },
    {
        question: "Q4. 네 번째 문제의 질문입니다.",
        image: "https://i.imgur.com/your_chat_image_4.png",
        options: [
            "A. 네 번째 선택지 1 내용입니다.",
            "B. 네 번째 선택지 2 내용입니다.",
            "C. 네 번째 선택지 3 내용입니다."
        ],
        correctAnswer: "A. 네 번째 선택지 1 내용입니다."
    },
    {
        question: "Q5. 마지막 문제의 질문입니다.",
        image: "https://i.imgur.com/your_chat_image_5.png",
        options: [
            "A. 마지막 선택지 1 내용입니다.",
            "B. 마지막 선택지 2 내용입니다.",
            "C. 마지막 선택지 3 내용입니다."
        ],
        correctAnswer: "B. 마지막 선택지 2 내용입니다."
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
    } else if (score >= 3) {
        resultMessage = "꽤 괜찮은 센스! 조금만 더 노력하면 완벽한 센스쟁이가 될 거예요. 😉";
    } else {
        resultMessage = "아직 센스가 부족하네요. 다음번엔 더 좋은 결과를 기대해 봐요! 🤔";
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