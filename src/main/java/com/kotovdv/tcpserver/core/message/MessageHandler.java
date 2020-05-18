package com.kotovdv.tcpserver.core.message;

public interface MessageHandler<T> {

    /**
     * Handles deserialized message.
     *
     * @param message Message.
     */
    void handle(T message);
}
