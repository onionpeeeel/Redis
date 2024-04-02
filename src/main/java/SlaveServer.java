import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SlaveServer implements Runnable{

    public void run() {
        System.out.println("Running Slave Thread");
        multiConnect();
    }

    private final Socket clientSocket;

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
                    clientSocket.getOutputStream().write(returnCommand("PING", "simple"));

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
