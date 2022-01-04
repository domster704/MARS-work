package com.amber.armtp;

/**
 * Singleton class for ftp-server
 */
public class ServerDetails {
    private static ServerDetails instance;

    public String host;
    public final String dir;
    public int port;

    public final String filePathInAndroid;

    private ServerDetails(String host, String dir, int port, String filePathInAndroid) {
        this.host = host;
        this.dir = dir;
        this.port = port;
        this.filePathInAndroid = filePathInAndroid;
    }

    public static ServerDetails getInstance() throws Exception {
        if (instance == null) throw new Exception("Wasn't created instance before");
        return instance;
    }

    public static ServerDetails getInstance(String host, String dir, int port, String filePathInAndroid) {
        if (instance == null) instance = new ServerDetails(host, dir, port, filePathInAndroid);
        return instance;
    }

    public static ServerDetails getInstance(String host, int port) {
        if (instance != null) {
            instance.host = host;
            instance.port = port;
        }
        return instance;
    }
}
