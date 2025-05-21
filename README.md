# Simple IRC System

## 1. Project Overview

A simple IRC system with a Java-based server, a Python command-line client, and a web interface (Node.js backend + HTML/JS/CSS frontend). Allows users to connect, set a nickname, and chat. No user registration is implemented.

## 2. Project Structure

-   `/irc_server`: Contains the Java IRC server.
-   `/irc_client`: Contains the Python command-line IRC client.
-   `/web_interface`: Contains the Node.js backend and HTML/JS/CSS frontend for the web chat.

## 3. Setup and Running Components

### Java IRC Server (`irc_server/`)

**Compilation:**
```bash
cd irc_server
javac src/main/java/com/example/ircserver/*.java -d out
```

**Running:**
```bash
cd irc_server
java -cp out com.example.ircserver.Server
```

**Note on Server Environment:**
During development, issues were encountered with the Java Virtual Machine initialization in the testing environment (`java.lang.InternalError: platform encoding not initialized` and `java.lang.InternalError: null property: user.dir`). This prevented the server from starting and being fully tested. The server code is provided as is and may require a stable Java environment (Java 8+ recommended) to run correctly.

**Functionality:**
Listens on port 6667 by default. Handles `NICK`, `PRIVMSG`, and `QUIT` commands.

### Python IRC Client (`irc_client/`)

**Prerequisites:**
Python 3.x.

**Running:**
```bash
cd irc_client
python client.py
```

**Functionality:**
Prompts for server IP (default `127.0.0.1`), port (default `6667`), and nickname. Connects to the IRC server, allows sending messages, and displays received messages. Type `/quit` to disconnect.

### Web Interface (`web_interface/`)

**Prerequisites:**
Node.js and npm.

**Setup:**
```bash
cd web_interface
npm install
```

**Running the Backend:**
```bash
cd web_interface
node server.js
```
The backend serves static files from the `public` directory and bridges WebSockets to the Java IRC server. It listens on port 3000 by default.

**Accessing the Frontend:**
Open a web browser and navigate to `http://localhost:3000`.

**Functionality:**
Allows setting a nickname and chatting. Messages are relayed through the Node.js backend to the Java IRC server.

## 4. Features

-   Set Nickname (no registration).
-   Send and receive messages in a common chat space.
-   Basic IRC command support (`NICK`, `PRIVMSG`, `QUIT`).
-   Command-line client.
-   Web-based client.

## 5. Limitations

-   No support for multiple channels (all messages are broadcast).
-   Minimal error handling in some parts.
-   No authentication or user accounts.
-   The Java IRC server component could not be fully tested in the development environment due to JVM issues.
