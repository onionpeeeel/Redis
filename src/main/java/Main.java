import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
  public final static String OK = "+PONG\r\n";
  public static void main(String[] args) {
//     You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");



    for (int i = 0; i < 2; i++ ) {
      Runnable runnable = new ThreadRunnable();
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }
}
