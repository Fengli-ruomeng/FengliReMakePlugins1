package com.example.ircserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    public static final int DEFAULT_PORT = 6667;
    private static final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("IRC Server started on port " + DEFAULT_PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + DEFAULT_PORT + ": " + e.getMessage());
        }
    }

    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public static synchronized void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public static synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public static synchronized boolean isNicknameTaken(String nickname) {
        if (nickname == null) {
            return false; 
        }
        for (ClientHandler clientHandler : clientHandlers) {
            String clientNickname = clientHandler.getNickname();
            if (clientNickname != null && clientNickname.equalsIgnoreCase(nickname)) {
                return true;
            }
        }
        return false;
    }
}
