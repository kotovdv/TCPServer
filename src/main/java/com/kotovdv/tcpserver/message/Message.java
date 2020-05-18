package com.kotovdv.tcpserver.message;

public final class Message {

    public final byte version;
    public final short messageType;
    public final int userId;
    public final String payload;

    public Message(byte version, short messageType, int userId, String payload) {
        this.version = version;
        this.messageType = messageType;
        this.userId = userId;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "version=" + version +
                ", messageType=" + messageType +
                ", userId=" + userId +
                ", payload='" + payload + '\'' +
                '}';
    }
}
