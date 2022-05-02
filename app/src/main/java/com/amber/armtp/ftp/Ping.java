package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

public class Ping {
    private final String host;
    private final String port;
    private final String name;
    private final String pass;
    private final int timeout = 7000;

    public Ping(ServerDetails serverDetails) {
        this.host = serverDetails.host;
        this.port = serverDetails.port;
        this.name = serverDetails.user;
        this.pass = serverDetails.password;
    }

//    public boolean isReachable() {
//        String ip = "ftp://" + name + ":" + pass + "@" + host + ":" + port + "/";
//        try {
//            URL url = new URL(ip);
//            URLConnection connection = url.openConnection();
//            connection.setConnectTimeout(timeout);
//            connection.connect();
//
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }

    public boolean isReachable() {
        SocketAddress socketAddress = new InetSocketAddress(host, Integer.parseInt(port));
        Socket socket = new Socket();
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
//        String ip = "ftp://" + name + ":" + pass + "@" + host + ":" + port + "/";
//        try {
//            URL url = new URL(ip);
//            URLConnection connection = url.openConnection();
//            connection.setConnectTimeout(7000);
//            connection.connect();
//
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
    }
}
