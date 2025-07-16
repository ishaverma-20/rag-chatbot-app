document.addEventListener('DOMContentLoaded', () => {
    const chatBox = document.getElementById('chat-box');
    const userInput = document.getElementById('user-input');
    const sendButton = document.getElementById('send-button');
    const ragSendButton = document.getElementById('rag-send-button');

    function addMessage(sender, message) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message');
        if (sender === 'user') {
            messageDiv.classList.add('user-message');
        } else {
            messageDiv.classList.add('bot-message');
        }
        messageDiv.textContent = message;
        chatBox.appendChild(messageDiv);
        chatBox.scrollTop = chatBox.scrollHeight; // Scroll to bottom
    }

    async function sendMessage(endpoint) {
        const message = userInput.value.trim();
        if (message) {
            addMessage('user', message);
            userInput.value = ''; // Clear input

            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ message: message })
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                addMessage('bot', data.response);
            } catch (error) {
                console.error('Error sending message:', error);
                addMessage('bot', 'Sorry, I am unable to respond at the moment.');
            }
        }
    }

    sendButton.addEventListener('click', () => sendMessage('/api/chat'));
    ragSendButton.addEventListener('click', () => sendMessage('/api/rag-chat'));

    userInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage('/api/chat');
        }
    });

    // Initial welcome message from the bot
    addMessage('bot', 'Hello! How can I help you today? Ask me anything.');
});