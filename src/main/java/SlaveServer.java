import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class SlaveServer implements Runnable{

    public void run() {
        System.out.println("Running Slave Thread");
        try {
            clientSocket = new Socket(redisProperties.getMasterNode(), Integer.parseInt(redisProperties.getMasterPort()));
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
        multiConnect();
    }

    public final static byte[] PONG = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] OK = "+OK\r\n".getBytes(StandardCharsets.UTF_8);

    public final static byte[] HEY = "+hey\r\n".getBytes(StandardCharsets.UTF_8);

    private final static TimedCache<String, String> commandList = new TimedCache<>();

    private Socket clientSocket;

    private final String role;

    private final RedisProperties redisProperties;

    public SlaveServer(Socket clientSocket, String role, RedisProperties redisProperties) {
        this.clientSocket = clientSocket;
        this.role = role;
        this.redisProperties = redisProperties;
    }

    public byte[] returnCommand(String command, String type) {
        String returnCommand = "";

        if ("simple".equals(type)) {
            returnCommand = "+" + command + "\r\n";
        } else if ("bulk".equals(type)) {
            returnCommand = "$" + command.length() + "\r\n" + command + "\r\n";
        }

        return returnCommand.getBytes(StandardCharsets.UTF_8);
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
                            if (storedCommand.size() > 6 && Objects.equals(Commands.PX, storedCommand.get(7))) {
                                if (storedCommand.size() > 8) {
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
                        case Commands.INFO:
                            if (storedCommand.size() > 3) {
                                String returnVal = "role:" + role + "\n" + "master_replid:" + redisProperties.getReplicationId() + "\n" +
                                        "master_repl_offset:" + redisProperties.getReplicationOffset();
                                clientSocket.getOutputStream().write(returnCommand(returnVal, "bulk"));
                            } else {
                                clientSocket.getOutputStream().write(returnCommand("role:" + role, "bulk"));
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
