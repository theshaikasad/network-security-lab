import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.shell.ShellFactory;
import java.io.*;

public class EchoShellFactory implements ShellFactory {
    @Override
    public Command createShell(ChannelSession channel) {
        return new EchoShell();
    }
}

class EchoShell implements Command {
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Thread thread;
    
    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }
    
    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }
    
    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }
    
    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        thread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
                PrintWriter writer = new PrintWriter(out, true);
                
                writer.println("Welcome to SSH Echo Server!");
                writer.println("Type 'exit' to quit");
                writer.flush();
                
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received (encrypted): " + line);
                    
                    if ("exit".equalsIgnoreCase(line.trim())) {
                        writer.println("Goodbye!");
                        writer.flush();
                        break;
                    }
                    
                    writer.println("Echo: " + line);
                    writer.flush();
                }
                
                callback.onExit(0);
            } catch (IOException e) {
                callback.onExit(1, e.getMessage());
            }
        });
        thread.start();
    }
    
    @Override
    public void destroy(ChannelSession channel) {
        if (thread != null) {
            thread.interrupt();
        }
    }
}