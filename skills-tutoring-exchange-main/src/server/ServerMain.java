package server;

import dao.DatabaseInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private static final int PORT = 5050;
    private static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Starting Server on port " + PORT + "...");

        DatabaseInitializer.initializeDatabase();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for clients.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                activeClients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends data directly to the client logged in as the given username.
     *
     * @return true if the user was found online and the data was sent,
     *         false if the user is not currently connected.
     */
    public static synchronized boolean sendToUser(String username, Object payload) {
        if (username == null) {
            return false;
        }

        for (ClientHandler client : activeClients) {
            String currentUsername = client.getCurrentUsername();
            if (username.equals(currentUsername)) {
                client.sendDataToClient(payload);
                return true;
            }
        }
        return false;
    }

    public static synchronized void removeClient(ClientHandler clientHandler) {
        activeClients.remove(clientHandler);
    }
}
