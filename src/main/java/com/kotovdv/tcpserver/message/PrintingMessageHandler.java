package com.kotovdv.tcpserver.message;

import com.kotovdv.tcpserver.core.message.MessageHandler;

import java.io.OutputStream;
import java.io.PrintStream;

public class PrintingMessageHandler implements MessageHandler<Message> {

    private final PrintStream outputStream;

    public PrintingMessageHandler(OutputStream outputStream) {
        this.outputStream = new PrintStream(outputStream);
    }

    @Override
    public void handle(Message message) {
        outputStream.println("Version: " + message.version);
        outputStream.println("Message Type: " + message.messageType);
        outputStream.println("User ID: " + message.userId);
        outputStream.println("Payload: " + message.payload);
    }
}
