const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const net = require('net');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Serve static files from 'public' directory
app.use(express.static('public'));

const IRC_PORT = 6667;
const IRC_HOST = '127.0.0.1';
const WEB_SERVER_PORT = 3000;

wss.on('connection', (wsClient) => {
    console.log('Web client connected');
    let nickname = null;
    let ircClient = null; // Will be initialized after wsClient connection

    // Create TCP client for IRC server
    ircClient = new net.Socket();

    ircClient.connect({ port: IRC_PORT, host: IRC_HOST }, () => {
        console.log(`Connected to IRC server at ${IRC_HOST}:${IRC_PORT}`);
        wsClient.send(JSON.stringify({ type: 'status', message: 'Connected to IRC bridge. Please provide a nickname using {"type": "nickname", "nickname": "your_nick"}' }));
    });

    wsClient.on('message', (message) => {
        let parsedMessage;
        try {
            parsedMessage = JSON.parse(message);
            console.log('Received from web client:', parsedMessage);
        } catch (e) {
            console.error('Failed to parse message from web client:', message);
            wsClient.send(JSON.stringify({ type: 'error', message: 'Invalid JSON message format.' }));
            return;
        }

        if (!nickname && parsedMessage.type === 'nickname') {
            nickname = parsedMessage.nickname;
            if (nickname && nickname.match(/^[a-zA-Z0-9_-]+$/)) {
                ircClient.write(`NICK ${nickname}\r\n`);
                console.log(`Setting IRC nickname to: ${nickname}`);
                // Server will send welcome message, which will be relayed
            } else {
                wsClient.send(JSON.stringify({ type: 'error', message: 'Invalid nickname format. Use letters, numbers, underscores, or hyphens.' }));
                nickname = null; // Reset nickname
            }
        } else if (nickname && parsedMessage.type === 'message') {
            const ircMessage = `PRIVMSG #webapp :${parsedMessage.text}\r\n`;
            ircClient.write(ircMessage);
            console.log(`Relaying message to IRC: ${ircMessage.trim()}`);
        } else if (!nickname) {
            wsClient.send(JSON.stringify({ type: 'error', message: 'Please set your nickname first. Use {"type": "nickname", "nickname": "your_nick"}' }));
        } else {
            wsClient.send(JSON.stringify({ type: 'error', message: `Unknown message type: ${parsedMessage.type} or invalid state.` }));
        }
    });

    ircClient.on('data', (data) => {
        const ircMessageString = data.toString().trim();
        console.log(`Received from IRC server: ${ircMessageString}`);
        wsClient.send(JSON.stringify({ type: 'irc_message', data: ircMessageString }));
    });

    ircClient.on('close', () => {
        console.log('Connection to IRC server closed');
        wsClient.send(JSON.stringify({ type: 'status', message: 'Disconnected from IRC server.' }));
        if (wsClient.readyState === WebSocket.OPEN) {
            wsClient.close();
        }
    });

    ircClient.on('error', (err) => {
        console.error('IRC client error:', err.message);
        wsClient.send(JSON.stringify({ type: 'error', message: `IRC connection error: ${err.message}` }));
        if (wsClient.readyState === WebSocket.OPEN) {
            wsClient.close();
        }
        if (ircClient && !ircClient.destroyed) {
            ircClient.destroy();
        }
    });

    wsClient.on('close', () => {
        console.log('Web client disconnected');
        if (ircClient && !ircClient.destroyed) {
            if (nickname) {
                ircClient.write(`QUIT :Web client disconnected\r\n`);
            }
            ircClient.destroy();
            console.log('IRC client connection ended due to web client disconnection.');
        }
    });

    wsClient.on('error', (err) => {
        console.error('Web client error:', err.message);
        if (ircClient && !ircClient.destroyed) {
            if (nickname) {
                ircClient.write(`QUIT :Web client error\r\n`);
            }
            ircClient.destroy();
            console.log('IRC client connection ended due to web client error.');
        }
    });
});

server.listen(WEB_SERVER_PORT, () => {
    console.log(`Web server listening on port ${WEB_SERVER_PORT}`);
});
