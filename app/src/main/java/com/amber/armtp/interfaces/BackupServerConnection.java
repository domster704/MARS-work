package com.amber.armtp.interfaces;

import com.amber.armtp.Config;
import com.amber.armtp.ServerDetails;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public interface BackupServerConnection {
    default boolean tryConnectToDefaultIpOtherwiseToBackupIp(FTPClient ftpClient) {
        try {
            ftpClient.connect(ServerDetails.getInstance().host);
        } catch (IOException e) {
            ServerDetails.getInstance().changeIpToBackupIpOrToDefaultIP();
            try {
                ftpClient.connect(ServerDetails.getInstance().host);
            } catch (IOException ignored) {
                ServerDetails.getInstance().changeIpToBackupIpOrToDefaultIP();
                Config.sout("Сервер недоступен");
                System.out.println("Сервер недоступен " + ServerDetails.getInstance().host);
                return false;
            }
        }
        return true;
    }

    default boolean tryConnectToDefaultIpOtherwiseToBackupIp(it.sauronsoftware.ftp4j.FTPClient ftpClient) {
        try {
            ftpClient.connect(ServerDetails.getInstance().host, Integer.parseInt(ServerDetails.getInstance().port));
        } catch (Exception e) {
            ServerDetails.getInstance().changeIpToBackupIpOrToDefaultIP();
            try {
                ftpClient.connect(ServerDetails.getInstance().host, Integer.parseInt(ServerDetails.getInstance().port));
            } catch (Exception ex) {
                ServerDetails.getInstance().changeIpToBackupIpOrToDefaultIP();
                Config.sout("Сервер недоступен");
                return false;
            }
        }
        return true;
    }
}
