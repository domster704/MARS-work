package com.amber.armtp.ftp;

import com.amber.armtp.Config;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.interfaces.BackupServerConnection;

import java.io.IOException;
import java.net.Socket;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPConnector;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class Ftp implements BackupServerConnection {
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

    public long getFileSize(String dir) throws FTPIllegalReplyException, FTPException, IOException {
        long size = 1;
        boolean login = initFtpClient();
        if (login) {
            size = client.fileSize(dir);
        }

        logout();
        return size;
    }

    public void deleteFile(String dir) throws IOException, FTPIllegalReplyException, FTPException {
        boolean login = initFtpClient();
        if (login) {
            client.deleteFile(dir);
        }
        logout();
    }

    public void renameFile(String dir) throws IOException, FTPIllegalReplyException, FTPException {
        boolean login = initFtpClient();
        if (login) {
            client.rename(dir, dir.split(".temp")[0] + ".dbf");
        }
        logout();
    }

    public static class MyFTPConnector extends FTPConnector {

        @Override
        public Socket connectForCommunicationChannel(String s, int i) throws IOException {
            return null;
        }

        @Override
        public Socket connectForDataTransferChannel(String s, int i) throws IOException {
            return null;
        }

        public int getTimeout() {
            return this.connectionTimeout;
        }
    }

    protected boolean initFtpClient() {
        client = new FTPClient();
        int timeout = 10 * 1000;
        client.setAutoNoopTimeout(timeout);

//        MyFTPConnector ftpConnector = new MyFTPConnector();
//        ftpConnector.setConnectionTimeout(timeout / 1000);
//        client.setConnector(ftpConnector);

        if (!tryConnectToDefaultIpOtherwiseToBackupIp(client)) {
            return false;
        }

        try {
//            client.connect(ServerDetails.getInstance().host, port);
            client.login(user, password);
            client.setType(FTPClient.TYPE_AUTO);

            return client.isAuthenticated();
        } catch (Exception e) {
            Config.sout("Время ожидания подключения истекло");
            e.printStackTrace();
        }
        return false;
    }

    protected void logout() throws FTPIllegalReplyException, IOException, FTPException {
        if (client.isAuthenticated()) {
            client.logout();
        }
        if (client.isConnected()) {
            client.disconnect(false);
        }
    }
}

//package com.amber.armtp.ftp;
//
//import com.amber.armtp.ServerDetails;
//
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//
//import java.io.IOException;
//
//public class Ftp {
//    protected final String user;
//    protected final String password;
//    protected final String host;
//    protected final int port;
//    protected FTPClient client;
//
//    public Ftp(ServerDetails serverDetails) {
//        this.host = serverDetails.host;
//        this.port = Integer.parseInt(serverDetails.port);
//        this.user = serverDetails.user;
//        this.password = serverDetails.password;
//    }
//
//    public long getFileSize(String dir) throws IOException {
//        boolean login = initFtpClient();
//        if (login) {
//            client.sendCommand("SIZE", dir);
//            return Long.parseLong(client.getReplyString().split(" ")[1].trim());
//        }
//
//        try {
//            client.logout();
//            client.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 1;
//    }
//
//    public boolean deleteFile(String dir) throws IOException {
//        boolean login = initFtpClient();
//        if (login) {
//            client.deleteFile(dir);
//        }
//        return client.completePendingCommand();
//    }
//
//    public void renameFile(String dir) throws IOException {
//        boolean login = initFtpClient();
//        if (login) {
//            client.rename(dir, dir.split(".temp")[0] + ".dbf");
//        }
//    }
//
//    protected boolean initFtpClient() {
//        this.client = new FTPClient();
//        try {
//            this.client.connect(host, port);
//            boolean login = this.client.login(user, password);
//
//            this.client.setFileType(FTP.BINARY_FILE_TYPE);
//            this.client.enterLocalPassiveMode();
////            this.client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
//
//            return login;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    protected void logout() throws IOException {
//        client.logout();
//        client.disconnect();
//    }
//}
