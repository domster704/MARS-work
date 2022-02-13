package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Ping {
    private final String host;
    private final String port;
    private final String name;
    private final String pass;

    public Ping(String host, String port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.name = user;
        this.pass = pass;
    }

    public Ping(ServerDetails serverDetails) {
        this.host = serverDetails.host;
        this.port = serverDetails.port;
        this.name = serverDetails.user;
        this.pass = serverDetails.password;
    }

    public boolean isReachable() {
        String ip = "ftp://" + name + ":" + pass + "@" + host + ":" + port + "/";
        try {
            URL url = new URL(ip);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(7000);
            connection.connect();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
