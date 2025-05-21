document.addEventListener('DOMContentLoaded', () => {
    // DOM Element References
    const nicknameInput = document.getElementById('nickname-input');
    const setNicknameButton = document.getElementById('set-nickname-button');
    const nicknameContainer = document.getElementById('nickname-container');
    const chatContainer = document.getElementById('chat-container');
    const messagesContainer = document.getElementById('messages'); // Renamed to avoid conflict
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');

    // WebSocket Setup
    const socket = new WebSocket('ws://' + window.location.host);

    // State Variable
    let currentNickname = null; // Renamed to avoid conflict with the function parameter

    // displayMessage function
    function displayMessage(type, text, source = '') {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add(type); // 'message', 'error', 'status', 'irc'

        if (type === 'message') {
            if (source === 'You') {
                messageDiv.classList.add('own');
            } else {
                messageDiv.classList.add('other');
            }
            messageDiv.innerHTML = `<strong>${source}:</strong> ${text}`;
        } else if (type === 'irc') {
            // Basic parsing for PRIVMSG
            const privmsgRegex = /^:([a-zA-Z0-9_-]+)!.*? PRIVMSG #webapp :(.+)$/;
            const match = text.match(privmsgRegex);
            if (match) {
                const senderNick = match[1];
                const messageContent = match[2];
                messageDiv.innerHTML = `<strong>${senderNick} (IRC):</strong> ${messageContent}`;
            } else {
                messageDiv.textContent = text; // Show raw IRC message if not PRIVMSG to #webapp
            }
        }
        else {
            messageDiv.textContent = text;
        }

        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // WebSocket Event Handlers
    socket.onopen = () => {
        console.log("WebSocket connection established.");
        displayMessage('status', 'Connected to server. Enter nickname to begin.');
    };

    socket.onmessage = (event) => {
        try {
            const msg = JSON.parse(event.data);
            console.log("Received from server:", msg);

            switch (msg.type) {
                case 'status':
                    displayMessage('status', msg.message);
                    break;
                case 'irc_message':
                    // Check if it's a welcome message for the current user
                    if (currentNickname && msg.data.includes(`:server 001 ${currentNickname}`)) {
                        displayMessage('status', `Welcome ${currentNickname}! You are now connected to IRC.`);
                    } else if (currentNickname && msg.data.startsWith(`:${currentNickname}!`)) {
                        // Don't display messages sent by the user themselves from IRC server
                        // (They are already displayed locally via displayMessage('message', messageValue, 'You'))
                        // This avoids duplicate messages if the Node.js bridge echoes back.
                        // However, the current Node.js server does not echo back PRIVMSG from the same user.
                        // This part might need adjustment based on exact server behavior.
                        console.log("Ignoring self-echoed IRC message:", msg.data);
                    }
                    else {
                        displayMessage('irc', msg.data);
                    }
                    break;
                case 'error':
                    displayMessage('error', msg.message);
                    break;
                default:
                    displayMessage('error', `Unknown message type received: ${msg.type}`);
                    break;
            }
        } catch (e) {
            console.error("Error parsing message from server or processing it:", e);
            displayMessage('error', 'Received an unparseable message from the server.');
        }
    };

    socket.onclose = () => {
        console.log("WebSocket connection closed.");
        displayMessage('status', 'Disconnected from server.');
        currentNickname = null; // Reset nickname on disconnect
        chatContainer.style.display = 'none';
        nicknameContainer.style.display = 'block';
    };

    socket.onerror = (error) => {
        console.error("WebSocket error:", error);
        displayMessage('error', 'Connection error. Please refresh the page.');
    };

    // Event Listener for "Set Nickname" Button
    setNicknameButton.addEventListener('click', () => {
        const nicknameValue = nicknameInput.value.trim();
        if (nicknameValue) {
            currentNickname = nicknameValue;
            socket.send(JSON.stringify({ type: 'nickname', nickname: currentNickname }));
            nicknameContainer.style.display = 'none';
            chatContainer.style.display = 'block';
            messageInput.focus();
            // Welcome message is now handled by server's 001 reply
        } else {
            displayMessage('error', 'Nickname cannot be empty.');
        }
    });
    
    nicknameInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            setNicknameButton.click();
        }
    });


    // Function to send message
    function sendMessage() {
        const messageValue = messageInput.value.trim();
        if (messageValue && currentNickname) {
            socket.send(JSON.stringify({ type: 'message', text: messageValue }));
            displayMessage('message', messageValue, 'You');
            messageInput.value = '';
        } else if (!currentNickname) {
            displayMessage('error', 'Please set your nickname first.');
        }
    }

    // Event Listener for "Send" Button
    sendButton.addEventListener('click', sendMessage);

    // Event Listener for Enter key on message input
    messageInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });
});
