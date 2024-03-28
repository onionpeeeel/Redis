import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadRunnable implements Runnable {
    public final static byte[] OK = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] HEY = "+hey\r\n".getBytes(StandardCharsets.UTF_8);

    private final Socket clientSocket;

    public ThreadRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Running Thread 1");
        multiConnect();
    }

    public byte[] returnCommand(String command) {
        String returnCommand = "+" + command + "\r\n";

        return returnCommand.getBytes(StandardCharsets.UTF_8);
    }

    public void multiConnect() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String readline = br.readLine();
                try {
                    if (readline.contains("ping")) {
                        clientSocket.getOutputStream().write(OK);
                    } else if (readline.contains("echo")) {
                        while (true) {
                            readline = br.readLine();
                            if (readline.startsWith("$")) {
                                continue;
                            }
                            clientSocket.getOutputStream().write(returnCommand(readline));
                        }

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
