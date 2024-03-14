package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.interfaces.BackupServerConnection;

import org.apache.commons.net.ftp.FTPClient;

public class Ping implements BackupServerConnection {
    public Ping() {
    }

    public boolean isReachable() {
        int timeout = ServerDetails.getInstance().timeout;
        FTPClient ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(timeout);
        ftpClient.setDataTimeout(timeout);
        ftpClient.setConnectTimeout(timeout);
        ftpClient.setControlKeepAliveTimeout(timeout);
        ftpClient.setControlKeepAliveReplyTimeout(timeout);

        return tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient);
    }
}
