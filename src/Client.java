import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;
    private static PrintWriter out;
    private static String username;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to the server.");
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Enter your username: ");
                username = scanner.nextLine();
                out.println(username);
                new Thread(new MessageReceiver(in)).start();

                String userInput;
                while (true) {
                    userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("exit")) {
                        break; 
                    }
                    out.println(userInput);
                }
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    static class MessageReceiver implements Runnable {
        private BufferedReader reader;

        public MessageReceiver(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("Server: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Error in receiving message: " + e.getMessage());
            }
        }
    }
}
