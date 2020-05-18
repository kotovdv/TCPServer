package com.kotovdv.tcpserver.core.server;

import com.kotovdv.tcpserver.core.exception.TCPServerException;
import com.kotovdv.tcpserver.core.message.MessageDeserializer;
import com.kotovdv.tcpserver.core.message.MessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class SocketConnectionHandler<T> {

    private static final int BUFFER_SIZE = 4096;
    private static final Logger log = LogManager.getLogger(SocketConnectionHandler.class);

    private final MessageHandler<T> handler;
    private final MessageDeserializer<T> deserializer;

    public SocketConnectionHandler(MessageHandler<T> handler,
                                   MessageDeserializer<T> deserializer) {
        this.handler = handler;
        this.deserializer = deserializer;
    }

    public void handle(Socket socket) {
        try (DataInputStream in = getInputStream(socket)) {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<T> request = deserializer.readNext(in);
                if (!request.isPresent())
                    return;

                handler.handle(request.get());
            }
        } catch (IOException e) {
            log.error("Failed to handle socket connection", e);
        } finally {
            close(socket);
        }
    }

    private DataInputStream getInputStream(Socket socket) {
        try {
            return new DataInputStream(new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE));
        } catch (IOException e) {
            throw new TCPServerException("Failed to acquire input stream from the socket", e);
        }
    }

    private void close(Socket socket) {
        if (socket.isClosed())
            return;
        try {
            socket.close();
        } catch (IOException e) {
            throw new TCPServerException("Failed to close socket", e);
        }
    }
}
