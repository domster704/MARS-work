package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public class Ftp {
    protected final String user;
    protected final String password;
    protected final String host;
    protected final int port;
    protected FTPClient client;

    public Ftp(ServerDetails serverDetails) {
        this.host = serverDetails.host;
        this.port = Integer.parseInt(serverDetails.port);
        this.user = serverDetails.user;
        this.password = serverDetails.password;
    }

    public long getFileSize(String dir) throws IOException {
        boolean login = initFtpClient();
        if (login) {
            client.sendCommand("SIZE", dir);
            return Long.parseLong(client.getReplyString().split(" ")[1].trim());
        }

        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public boolean deleteFile(String dir) throws IOException {
        boolean login = initFtpClient();
        if (login) {
            client.deleteFile(dir);
        }
        return client.completePendingCommand();
    }

    public void renameFile(String dir) throws IOException {
        boolean login = initFtpClient();
        if (login) {
            client.rename(dir, dir.split(".temp")[0] + ".dbf");
        }
    }

    protected boolean initFtpClient() {
        this.client = new FTPClient();
        try {
            this.client.connect(host, port);
            boolean login = this.client.login(user, password);

            this.client.enterLocalPassiveMode();
            this.client.setFileType(FTP.BINARY_FILE_TYPE);
            this.client.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            return login;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
