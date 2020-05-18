package com.kotovdv.tcpserver.core.server;

import com.kotovdv.tcpserver.core.exception.TCPServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    private final int serverPort;
    private final int socketBacklog;
    private final int maxConnections;
    private final SocketConnectionHandler<?> handler;
    private final SocketConnectionDispatcher dispatcher = new SocketConnectionDispatcher();
    private final Object lock = new Object();

    private volatile boolean started;
    private ExecutorService executor;
    private ServerSocket serverSocket;

    public Server(int port,
                  int maxConnections,
                  SocketConnectionHandler<?> handler) {
        this(port, maxConnections, maxConnections, handler);
    }

    public Server(int port,
                  int socketBacklog,
                  int maxConnections,
                  SocketConnectionHandler<?> handler) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("Illegal port value " + port);
        this.serverPort = port;

        if (socketBacklog < 0)
            throw new IllegalArgumentException("Illegal socket backlog value " + socketBacklog);
        this.socketBacklog = socketBacklog;

        if (maxConnections < 1)
            throw new IllegalArgumentException("Illegal max connections value " + maxConnections);
        this.maxConnections = maxConnections;

        this.handler = Objects.requireNonNull(handler, "Socket connection handler can not be null");
    }

    public void start() {
        if (started)
            return;
        synchronized (lock) {
            if (started)
                return;
            doStart();
            started = true;
        }
    }

    public void stop(long timeout, TimeUnit unit) {
        if (!started)
            return;
        synchronized (lock) {
            if (!started)
                return;
            doStop(timeout, unit);
            started = false;
        }
    }

    private void doStart() {
        log.info("Attempting to start TCP Server on port [{}]", serverPort);

        try {
            this.serverSocket = new ServerSocket(serverPort, socketBacklog);
        } catch (IOException e) {
            throw new TCPServerException("Failed to open server socket at port: " + serverPort, e);
        }

        log.info("Server socket initialized");

        this.executor = new ThreadPoolExecutor(
                maxConnections + 1, maxConnections + 1,
                0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        this.executor.execute(() -> dispatcher.dispatch(serverSocket, executor, handler));
        log.info("Workers initialized");

        log.info("Server started");
    }

    private void doStop(long timeout, TimeUnit unit) {
        log.info("Attempting to stop TCP Server");
        log.info("Shutting down workers");
        try {
            this.executor.shutdownNow();
            executor.awaitTermination(timeout, unit);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("WTF???", e);
        }

        log.info("Shutting down server socket");
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Failed to close server socket", e);
        }

        log.info("Server stopped");
    }
}
