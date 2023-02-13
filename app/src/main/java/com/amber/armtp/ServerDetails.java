package com.amber.armtp;

/**
 * Singleton class for ftp-server
 */
public class ServerDetails {
    private static ServerDetails instance;

    public int timeout = 15 * 1000;
    public String user;
    public String password;
    public String host;
    public String dirDB;
    public String dirAPK;
    public String port;

    private String backupIp = "";
    private String previousHost = "";

    private ServerDetails(String... args) {
        this.host = args[0];
        this.dirDB = args[1];
        this.port = args[2];
        this.user = args[3];
        this.password = args[4];
        this.dirAPK = args[5];
    }

    public static ServerDetails getInstance() {
        if (instance == null) return null;
        return instance;
    }

    public static ServerDetails getInstance(String... args) {
        if (instance == null) instance = new ServerDetails(args);
        return instance;
    }

    public static void updateInstance(String host, String port, String user, String pass) {
        if (instance != null) {
            instance.host = host;
            instance.port = port;
            instance.user = user;
            instance.password = pass;
        }
    }

    private static int count = 0;

    public void changeIpToBackupIpOrToDefaultIP() {
        if (count % 2 == 0) {
            previousHost = host;
            host = backupIp;
        } else {
            host = previousHost;
        }
        count++;
    }

    public void setBackupIp(String backupIp) {
        this.backupIp = backupIp;
    }

    public String getBackUpIp() {
        return this.backupIp;
    }
}
