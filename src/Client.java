import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;
    private static PrintWriter out;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to the server.");
            new Thread(new MessageReceiver(socket)).start();

            out = new PrintWriter(socket.getOutputStream(), true);
            try (Scanner scanner = new Scanner(System.in)) {
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
        private Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("SERVER RESPONSE BACK: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Error in receiving message: " + e.getMessage());
            }
        }
    }
}
