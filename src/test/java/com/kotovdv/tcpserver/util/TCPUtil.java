package com.kotovdv.tcpserver.util;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPUtil {

    public static int findFreePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int localPort = serverSocket.getLocalPort();
            serverSocket.close();

            return localPort;
        } catch (IOException e) {
            throw new RuntimeException("Failed to find free port", e);
        }
    }
}
