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
        System.out.println("--------------------------1");
        clientSocket.getOutputStream().write("*1\r\n$4\r\nping\r\n".getBytes(StandardCharsets.UTF_8));
        clientSocket.getOutputStream().write(Commands.replconf(port).getBytes(StandardCharsets.UTF_8));
        clientSocket.getOutputStream().write("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n".getBytes(StandardCharsets.UTF_8));
        clientSocket.getOutputStream().write("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n".getBytes(StandardCharsets.UTF_8));
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
    }

//    while (true) {
//      Runnable runnable = new ThreadRunnable();
//      Thread thread = new Thread(runnable);
//      thread.start();
//    }
  }
}
