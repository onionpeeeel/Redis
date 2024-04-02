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

      while (true) {
        clientSocket = serverSocket.accept();
        System.out.println("Got connection with " + clientSocket.getPort());
        if ("master".equals(role)) {
          executorService.submit(new MasterServer(clientSocket, role, redisProperties));
        } else {
//          clientSocket = new Socket(redisProperties.getMasterNode(),
//                  Integer.parseInt(redisProperties.getMasterPort()));
//          clientSocket.getOutputStream().write("*1\r\n$4\r\nping\r\n".getBytes(StandardCharsets.UTF_8));
          executorService.submit(new SlaveServer(clientSocket, role, redisProperties));
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
