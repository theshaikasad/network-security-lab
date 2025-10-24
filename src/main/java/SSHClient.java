import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import java.io.*;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SSHClient {
    public static void main(String[] args) throws IOException {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        
        try (ClientSession session = client.connect("admin", "localhost", 2222)
                .verify(10, TimeUnit.SECONDS).getSession()) {
            
            session.addPasswordIdentity("password");
            session.auth().verify(10, TimeUnit.SECONDS);
            
            System.out.println("Connected to SSH server");
            
            try (ClientChannel channel = session.createShellChannel()) {
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                
                PipedOutputStream pipedIn = new PipedOutputStream();
                PipedInputStream inputStream = new PipedInputStream(pipedIn);
                
                channel.setIn(inputStream);
                channel.setOut(responseStream);
                channel.setErr(responseStream);
                
                channel.open().verify(5, TimeUnit.SECONDS);
                
                // Thread to read responses
                Thread readerThread = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[1024];
                        while (channel.isOpen()) {
                            if (responseStream.size() > 0) {
                                String response = responseStream.toString();
                                if (!response.isEmpty()) {
                                    System.out.print(response);
                                    responseStream.reset();
                                }
                            }
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                readerThread.start();
                
                // Send commands
                Scanner scanner = new Scanner(System.in);
                PrintWriter writer = new PrintWriter(pipedIn, true);
                
                while (channel.isOpen()) {
                    String input = scanner.nextLine();
                    writer.println(input);
                    
                    if ("exit".equalsIgnoreCase(input.trim())) {
                        break;
                    }
                }
                
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 5000);
                scanner.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.stop();
        }
    }
}