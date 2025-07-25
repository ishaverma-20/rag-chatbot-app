document.addEventListener('DOMContentLoaded', () => {
    const chatBox = document.getElementById('chat-box');
    const userInput = document.getElementById('user-input');
    const ragSendButton = document.getElementById('rag-send-button');
    const normalSendButton = document.getElementById('normal-send-button');
    const authContainer = document.getElementById('auth-container');

    // Initialize the Showdown converter
    const converter = new showdown.Converter();

    async function checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/status');
            const data = await response.json();
            if (data.isAuthenticated) {
                authContainer.innerHTML = '<a href="/logout">Logout</a>';
            } else {
                authContainer.innerHTML = '<a href="/login">Login</a>';
            }
        } catch (error) {
            console.error("Auth check failed:", error);
            authContainer.innerHTML = '<a href="/login">Login</a>';
        }
    }

    function addMessage(sender, message) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', sender === 'user' ? 'user-message' : 'bot-message');

        // If the message is from the bot, convert it from Markdown to HTML
        if (sender === 'bot') {
            const html = converter.makeHtml(message);
            messageDiv.innerHTML = html;
        } else {
            messageDiv.textContent = message;
        }

        chatBox.appendChild(messageDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    async function sendMessage(endpoint) {
        const message = userInput.value.trim();
        if (message) {
            addMessage('user', message);
            userInput.value = '';
            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: message })
                });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                const data = await response.json();
                addMessage('bot', data.response);
            } catch (error) {
                console.error('Error sending message:', error);
                addMessage('bot', 'Sorry, an error occurred.');
            }
        }
    }

    ragSendButton.addEventListener('click', () => sendMessage('/api/rag-chat'));
    normalSendButton.addEventListener('click', () => sendMessage('/api/chat'));

    userInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage('/api/rag-chat');
        }
    });

    checkAuthStatus();
});