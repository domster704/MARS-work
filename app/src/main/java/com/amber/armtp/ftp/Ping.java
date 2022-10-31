package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.interfaces.BackupServerConnection;

import org.apache.commons.net.ftp.FTPClient;

public class Ping implements BackupServerConnection {
    private final String host;
    private final String port;
    private final String name;
    private final String pass;

//    private final String backupIp;

    public Ping(ServerDetails serverDetails) {
        this.host = serverDetails.host;
        this.port = serverDetails.port;
        this.name = serverDetails.user;
        this.pass = serverDetails.password;
//        this.backupIp = serverDetails.backupIp;
    }

    public boolean isReachable() {
//        System.out.println(12345);
//        String ip = "ftp://" + name + ":" + pass + "@" + host + ":" + port + "/";
        int timeout = 5000;
        FTPClient ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(timeout);
        ftpClient.setDataTimeout(timeout);
        ftpClient.setConnectTimeout(timeout);
        ftpClient.setControlKeepAliveTimeout(timeout);
        ftpClient.setControlKeepAliveReplyTimeout(timeout);

        return tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient);

//        try {
//            URLConnection connection = (HttpURLConnection) new URL(ip).openConnection();
//            connection.setConnectTimeout(timeout);
//            connection.set("HEAD");
//            int responseCode = connection.getResponseCode();
//            if (responseCode != 200) {
//                ip = "ftp://" + name + ":" + pass + "@" + backupIp + ":" + port + "/";
//                connection = (HttpURLConnection) new URL(ip).openConnection();
//                connection.setConnectTimeout(timeout);
//                connection.setRequestMethod("HEAD");
//                responseCode = connection.getResponseCode();
//                if (responseCode != 200) {
//                    return false;
//                }
//            }
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }


//        int timeout = 99;
//        try {
//            URL url = new URL(ip);
//            URLConnection connection = url.openConnection();
//            connection.setConnectTimeout(timeout);
//            connection.connect();
//            return true;
//        } catch (IOException e) {
//            System.out.println(122222);
//            try {
//                ip = "ftp://" + name + ":" + pass + "@" + backupIp + ":" + port + "/";
//                URL url = new URL(ip);
//                URLConnection connection = url.openConnection();
//                connection.setConnectTimeout(timeout);
//                connection.connect();
//                return true;
//            } catch (IOException ignore) {}
//        }
//        System.out.println(123456);
//        return false;
    }
}
