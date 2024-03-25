import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadRunnable implements Runnable {
    public final static String OK = "+PONG\r\n";

    public final static int port = 6379;

    public static ServerSocket serverSocket;

    static {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        System.out.println("Running Thread 1");
        multiConnect();
    }

    public void multiConnect() {
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String readline = br.readLine();
                try {
                    if (readline.contains("ping")) {
                        clientSocket.getOutputStream().write(OK.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
