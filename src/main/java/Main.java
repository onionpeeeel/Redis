import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
  public final static String OK = "+PONG\r\n";
  public static void main(String[] args){
//     You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;
    try {
      // 서버 생성
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);
      // Client 접속 Accept
      clientSocket = serverSocket.accept();

      // client 가 보낸 message
      // DataInputStream is = new DataInputStream(clientSocket.getInputStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      // client 에 data 전송
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      bw.write(OK);
      bw.newLine();
      bw.flush();
      bw.close();

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
