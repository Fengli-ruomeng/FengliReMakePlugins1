package com.example.ircserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error initializing client handler streams: " + e.getMessage());
            // Optionally, close the socket if streams can't be initialized
             try {
                if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                    this.clientSocket.close();
                }
            } catch (IOException ex) {
                System.err.println("Error closing client socket after initialization failure: " + ex.getMessage());
            }
        }
    }

    @Override
    public void run() {
        if (out == null || in == null) {
            // Streams were not initialized properly, cannot proceed
            return;
        }

        Server.addClient(this);
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from " + (nickname != null ? nickname : clientSocket.getInetAddress().getHostAddress()) + ": " + inputLine);
                if (inputLine.toUpperCase().startsWith("NICK ")) {
                    String potentialNickname = inputLine.substring(5).trim();
                    if (potentialNickname.isEmpty() || !potentialNickname.matches("[a-zA-Z0-9_-]+")) {
                        sendMessage(":server 432 * " + potentialNickname + " :Erroneous nickname");
                    } else if (Server.isNicknameTaken(potentialNickname)) {
                        sendMessage(":server 433 * " + potentialNickname + " :Nickname is already in use");
                    } else {
                        this.nickname = potentialNickname;
                        sendMessage(":server 001 " + nickname + " :Welcome to the IRC Server " + nickname);
                    }
                } else if (inputLine.toUpperCase().startsWith("PRIVMSG ")) {
                    if (nickname == null) {
                        sendMessage(":server 431 :No nickname given");
                        continue;
                    }
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage(":server 461 " + (nickname != null ? nickname : "*") + " PRIVMSG :Not enough parameters");
                        continue;
                    }
                    String target = parts[1];
                    String messageContent = parts[2].startsWith(":") ? parts[2].substring(1) : parts[2];
                    String formattedMessage = ":" + nickname + " PRIVMSG " + target + " :" + messageContent;
                    Server.broadcastMessage(formattedMessage, this);
                } else if (inputLine.equalsIgnoreCase("QUIT")) {
                    break; // Client initiated quit
                }
                // Handle other commands or ignore
            }
        } catch (IOException e) {
            System.err.println("Error handling client " + (nickname != null ? nickname : clientSocket.getInetAddress().getHostAddress()) + ": " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources for client " + (nickname != null ? nickname : clientSocket.getInetAddress().getHostAddress()) + ": " + e.getMessage());
            }
            Server.removeClient(this);
            if (nickname != null) {
                String quitMessage = ":" + nickname + " QUIT :Client disconnected";
                Server.broadcastMessage(quitMessage, this); // 'this' is already removed, so it won't receive it.
                System.out.println("Client " + nickname + " disconnected.");
            } else {
                System.out.println("Client " + clientSocket.getInetAddress().getHostAddress() + " disconnected.");
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getNickname() {
        return nickname;
    }
}
