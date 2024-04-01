import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public final static String OK = "+PONG\r\n";
  public static void main(String[] args) throws IOException{
//     You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    System.out.println(Arrays.toString(args));

    Socket clientSocket;
    int port = 6379;

    if ("--port".equals(args[0])) {
      port = Integer.parseInt(args[1]);
    }

    try (ServerSocket serverSocket = new ServerSocket(port);
         ExecutorService executorService = Executors.newFixedThreadPool(8)) {
      serverSocket.setReuseAddress(true);

      while (true) {
        clientSocket = serverSocket.accept();
        System.out.println("Got connection with " + clientSocket.getPort());
        executorService.submit(new ThreadRunnable(clientSocket));
      }
    }



//    while (true) {
//      Runnable runnable = new ThreadRunnable();
//      Thread thread = new Thread(runnable);
//      thread.start();
//    }
  }
}
