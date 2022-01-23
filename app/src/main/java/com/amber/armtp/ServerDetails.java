package com.amber.armtp;

/**
 * Singleton class for ftp-server
 */
public class ServerDetails {
    private static ServerDetails instance;

    public String user;
    public String password;
    public String host;
    public String dirDB;
    public String dirAPK;
    public String port;

    private ServerDetails(String... args) {
        this.host = args[0];
        this.dirDB = args[1];
        this.port = args[2];
        this.user = args[3];
        this.password = args[4];
        this.dirAPK = args[5];
    }

    public static ServerDetails getInstance() throws Exception {
        if (instance == null) throw new Exception("Wasn't created instance before");
        return instance;
    }

    public static ServerDetails getInstance(String... args) {
        if (instance == null) instance = new ServerDetails(args);
        return instance;
    }

    public static ServerDetails getInstance(String host, String port) {
        if (instance != null) {
            instance.host = host;
            instance.port = port;
        }
        return instance;
    }
}
