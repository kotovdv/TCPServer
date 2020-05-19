package com.kotovdv.tcpserver.core.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Dispatches incoming socket connections to available threads for handling.
 */
public class SocketConnectionDispatcher {

    private static final Logger log = LogManager.getLogger(SocketConnectionDispatcher.class);

    public void dispatch(ServerSocket serverSocket,
                         ExecutorService executor,
                         SocketConnectionHandler<?> connectionHandler) {

        Socket socket = null;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = serverSocket.accept();
                submit(socket, executor, connectionHandler);
            } catch (RejectedExecutionException e) {
                handleServerCapacityExceeded(socket);
            } catch (IOException e) {
                log.error("Failed to accept client connection", e);
                return;
            }
        }
    }

    private void submit(Socket connection,
                        ExecutorService executor,
                        SocketConnectionHandler<?> connectionHandler) {
        executor.execute(() -> connectionHandler.handle(connection));
    }

    private void handleServerCapacityExceeded(Socket socket) {
        log.error("Capacity exceeded, connection aborted");
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Failed to close socket", e);
            }
        }
    }
}
