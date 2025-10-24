import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TelnetClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 2323;
        
        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to server");
            
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            
            // Start thread to read server responses
            new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println("Server: " + response);
                    }
                } catch (IOException ex) {
                    System.out.println("Connection closed");
                }
            }).start();
            
            // Read user input and send to server
            Scanner scanner = new Scanner(System.in);
            String userInput;
            
            while (true) {
                userInput = scanner.nextLine();
                writer.println(userInput);
                
                if ("exit".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }
            
            scanner.close();
            
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}