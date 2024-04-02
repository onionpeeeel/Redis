public class Commands {
    public static final String ECHO = "echo";

    public static final String PING = "ping";

    public static final String SET = "set";

    public static final String GET = "get";

    public static final String PX = "px";

    public static final String INFO = "info";

    public static final String REPLCONF = "replconf";

    public static final String CAPA = "capa";

    public static final String PSYNC = "psync";

    public static String replconf(int reqPort) {
        String port = String.valueOf(reqPort);

        return "*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n" + port + "\r\n";
    }
}
