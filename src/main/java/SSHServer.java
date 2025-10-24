import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import java.io.IOException;
import java.nio.file.Paths;

public class SSHServer {
    public static void main(String[] args) throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2222);
        
        // Set host key
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
            Paths.get("hostkey.ser")));
        
        // Set password authenticator
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, 
                                       ServerSession session) {
                // Simple authentication - username: admin, password: password
                return "admin".equals(username) && "password".equals(password);
            }
        });
        
        // Set shell factory (echo shell)
        sshd.setShellFactory(new EchoShellFactory());
        
        sshd.start();
        System.out.println("SSH Server started on port 2222");
        System.out.println("Username: admin");
        System.out.println("Password: password");
        System.out.println("Press Ctrl+C to stop");
        
        // Keep the server running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }
    }
}