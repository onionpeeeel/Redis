import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadRunnable implements Runnable {
    public final static byte[] PONG = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] OK = "+OK\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] HEY = "+hey\r\n".getBytes(StandardCharsets.UTF_8);

    public static Map<String, String> commandList = new HashMap<>();

    private final Socket clientSocket;

    public ThreadRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Running Thread 1");
        multiConnect();
    }

    public byte[] returnCommand(String command, String type) {
        String returnCommand = "";

        if ("simple".equals(type)) {
            returnCommand = "+" + command + "\r\n";
        } else if ("bulk".equals(type)) {
            returnCommand = "$"+ command.length() + "\r\n" + command + "\r\n";
        }

        return returnCommand.getBytes(StandardCharsets.UTF_8);
    }

    public void commandSave(String key, String value) {
        commandList.put(key, value);
    }

    public void multiConnect() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String input = br.readLine();

            while (input != null && !input.isEmpty()) {
                if (input.startsWith("*")) {
                    int numOfLines = Integer.parseInt(String.valueOf(input.charAt(1)));
                    ArrayList<String> storedCommand = new ArrayList<>(numOfLines * 2);
                    for (int i = 0; i < numOfLines * 2; i++) {
                        storedCommand.add(br.readLine());
                    }
                    String command = storedCommand.get(1);
                    switch (command.toLowerCase()) {
                        case Commands.PING:
                            clientSocket.getOutputStream().write(PONG);
                            break;
                        case Commands.ECHO:
                            clientSocket.getOutputStream().write(returnCommand(storedCommand.get(3), "simple"));
                            break;
                        case Commands.SET:
                            String key = storedCommand.get(3);
                            String value = storedCommand.get(5);
                            commandList.put(key, value);
                            clientSocket.getOutputStream().write(OK);
                            break;
                        case Commands.GET:
                            String getKey = storedCommand.get(3);
                            String getValue = commandList.get(getKey);
                            if (getValue == null) {
                                clientSocket.getOutputStream().write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
                            } else {
                                clientSocket.getOutputStream().write(returnCommand(getValue, "bulk"));
                            }
                            break;
                    }
                }
                input = br.readLine();
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
