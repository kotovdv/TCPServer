package com.kotovdv.tcpserver;

import com.kotovdv.tcpserver.core.server.Server;
import com.kotovdv.tcpserver.core.server.SocketConnectionHandler;
import com.kotovdv.tcpserver.message.DefaultMessageDeserializer;
import com.kotovdv.tcpserver.message.PrintingMessageHandler;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Server server = new Server(8087, 16, new SocketConnectionHandler<>(
                new PrintingMessageHandler(System.out),
                new DefaultMessageDeserializer()
        ));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(3, TimeUnit.SECONDS);
            LogManager.shutdown();
        }));

        server.start();
    }
}
