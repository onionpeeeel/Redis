import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public final static String OK = "+PONG\r\n";
  public static void main(String[] args) throws IOException{
//     You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    System.out.println(Arrays.toString(args));

    RedisProperties redisProperties = new RedisProperties();

    Socket clientSocket;
    PrintWriter writeClient;
    BufferedReader readClient;
    int port = 6379;
    String role = "master";

    if (args.length > 0 && Objects.equals(args[0], "--port")) {
      port = Integer.parseInt(args[1]);
      if (args.length > 2 && Objects.equals(args[2], "--replicaof")) {
        role = "slave";
        if (args.length > 3) {
          redisProperties.setMasterNode(args[3]);
          if (args.length > 4) {
            redisProperties.setMasterPort(args[4]);
          }
        }
      }
    }

    try (ServerSocket serverSocket = new ServerSocket(port);
         ExecutorService executorService = Executors.newFixedThreadPool(8)) {
      serverSocket.setReuseAddress(true);

      if (!Objects.equals("master", role)) {
        clientSocket = new Socket(redisProperties.getMasterNode(),
                          Integer.parseInt(redisProperties.getMasterPort()));
        writeClient = new PrintWriter(clientSocket.getOutputStream(), true);
        readClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("--------------------------1");
        writeClient.write(toArrayRESP(toBulkString("ping")));
        writeClient.flush();
        String resp = readClient.readLine();
        System.out.println(resp);
        sendCommand(writeClient, toArrayRESP(toBulkString("REPLCONF"),
                toBulkString("listening-port"),
                toBulkString("6380")));
        resp = readClient.readLine();
        System.out.println(resp);
        sendCommand(writeClient, toArrayRESP(toBulkString("REPLCONF"),
                toBulkString("capa"),
                toBulkString("psync2")));
        resp = readClient.readLine();
        System.out.println(resp);
        sendCommand(writeClient, toArrayRESP(toBulkString("PSYNC"),
                toBulkString("?"),
                toBulkString("-1")));
        resp = readClient.readLine();
        System.out.println(resp);
      }

      while (true) {
        System.out.println("---------------------------2");
        clientSocket = serverSocket.accept();
        System.out.println("Got connection with " + clientSocket.getPort());
        if ("master".equals(role)) {
          System.out.println("---------------------------3");
          executorService.submit(new MasterServer(clientSocket, role, redisProperties));
        } else {
          System.out.println("---------------------------4");
          executorService.submit(new SlaveServer(clientSocket, role, redisProperties, port));
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

//    while (true) {
//      Runnable runnable = new ThreadRunnable();
//      Thread thread = new Thread(runnable);
//      thread.start();
//    }
  }

  private static void sendCommand(PrintWriter writeClient, String command) {
    writeClient.write(command);
    writeClient.flush();
  }

  private static String toArrayRESP(String... respStrings) {
    return "*" + respStrings.length + "\r\n" + String.join("", respStrings);
  }

  private static String toBulkString(String command) {
    return String.join("\r\n", "$" + command.length(), command, "");
  }
}
