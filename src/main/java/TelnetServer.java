import java.io.*;
import java.net.*;

public class TelnetServer {
    private static final int PORT = 2323;
    
    public static void main(String[] args) {
        System.out.println("Telnet Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                
                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;
    
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            
            writer.println("Welcome to Telnet Server!");
            writer.println("Type 'exit' to quit");
            
            String text;
            
            while ((text = reader.readLine()) != null) {
                System.out.println("Received: " + text);
                
                if ("exit".equalsIgnoreCase(text.trim())) {
                    writer.println("Goodbye!");
                    break;
                }
                
                writer.println("Echo: " + text);
            }
            
            socket.close();
            System.out.println("Client disconnected");
            
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}