import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
            returnCommand = "$3\r\n" + command + "\r\n";
        }

        return returnCommand.getBytes(StandardCharsets.UTF_8);
    }

    public void commandSave(String key, String value) {
        commandList.put(key, value);
    }

    public void multiConnect() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String readline = br.readLine();
                try {
                    if (readline.contains("ping")) {
                        clientSocket.getOutputStream().write(PONG);
                    } else if (readline.contains("echo")) {
                        while (true) {
                            readline = br.readLine();
                            if (readline.startsWith("$")) {
                                continue;
                            }
                            clientSocket.getOutputStream().write(returnCommand(readline, "simple"));
                        }

                    } else if (readline.contains("set")) {
                        while (true) {
                            readline = br.readLine();
                            if (readline.startsWith("$")) {
                                continue;
                            } else {
                                String key = readline;
                                readline = br.readLine();
                                if (readline.startsWith("$")) {
                                    readline = br.readLine();
                                    if (!readline.startsWith("$")) {
                                        String value = readline;
                                        commandSave(key, value);
                                        clientSocket.getOutputStream().write(OK);
                                    }
                                }
                            }
                        }
                    } else if (readline.contains("get")) {
                        while (true) {
                            readline = br.readLine();
                            if (readline.startsWith("$")) {
                                continue;
                            } else {
                                System.out.println(readline);
                                if (commandList.containsKey(readline)) {
                                    clientSocket.getOutputStream().write(returnCommand(commandList.get(readline), "bulk"));
                                }
                            }
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
