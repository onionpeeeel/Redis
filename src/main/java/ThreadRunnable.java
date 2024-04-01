import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadRunnable implements Runnable {
    public final static byte[] PONG = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] OK = "+OK\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] HEY = "+hey\r\n".getBytes(StandardCharsets.UTF_8);

//    public static ConcurrentHashMap<String, String> commandList = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> expireCommandList = new ConcurrentHashMap<>();

    private final Socket clientSocket;

    private final static TimedCache<String, String> commandList = new TimedCache<>();
    private final String role;

    public ThreadRunnable(Socket clientSocket, String role) {
        this.clientSocket = clientSocket;
        this.role = role;
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
        commandList.put(key, value, 0);
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
                    System.out.println(storedCommand);
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
                            if (storedCommand.size() > 6) {
                                if (Commands.PX.equals(storedCommand.get(7))) {
                                    commandList.put(key, value, Long.parseLong(storedCommand.get(9)));
                                } else {
                                    commandList.put(key, value, -1);
                                }
                            } else {
                                commandList.put(key, value, -1);
                            }

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
                        case Commands.INFO:
                            clientSocket.getOutputStream().write(returnCommand("role:" + role, "bulk"));
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
