import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();
    private static List<String> messageHistory = new ArrayList<>();
    private static final int MESSAGE_HISTORY_LIMIT = 10;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void broadcast(String message) {
        for (PrintWriter writer : clientWriters.values()) {
            writer.println(message);
        }
    }

    private static void sendPrivateMessage(String sender, String recipient, String message) {
        PrintWriter writer = clientWriters.get(recipient);
        if (writer != null) {
            writer.println(sender + " (private): " + message);
        } else {
            PrintWriter senderWriter = clientWriters.get(sender);
            if (senderWriter != null) {
                senderWriter.println("User " + recipient + " not found.");
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your username: ");
                username = in.readLine();
                synchronized (clientWriters) {
                    clientWriters.put(username, out);
                    System.out.println("User " + username + " has joined.");
                    broadcast(username + " has joined the chat.");
                }

                for (String message : messageHistory) {
                    out.println(message);
                }

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if (clientMessage.startsWith("/msg")) {
                        String[] parts = clientMessage.split(" ", 3);
                        if (parts.length == 3) {
                            sendPrivateMessage(username, parts[1], parts[2]);
                        } else {
                            out.println("Usage: /msg <username> <message>");
                        }
                    } else {
                        // Broadcast message
                        messageHistory.add(username + ": " + clientMessage);
                        if (messageHistory.size() > MESSAGE_HISTORY_LIMIT) {
                            messageHistory.remove(0);
                        }
                        System.out.println("Received from " + username + ": " + clientMessage);
                        broadcast(username + ": " + clientMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (username != null) {
                        synchronized (clientWriters) {
                            clientWriters.remove(username);
                            broadcast(username + " has left the chat.");
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
