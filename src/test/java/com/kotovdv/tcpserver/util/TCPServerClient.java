package com.kotovdv.tcpserver.util;

import com.kotovdv.tcpserver.message.DefaultMessageDeserializer;
import com.kotovdv.tcpserver.message.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPServerClient {

    private final Socket socket;
    private final DataOutputStream dataOutputStream;

    private TCPServerClient(Socket socket,
                            DataOutputStream dataOutputStream) {
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
    }

    public static TCPServerClient create(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            return new TCPServerClient(socket, new DataOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create tcp server client ", e);
        }
    }

    public void sendMessage(Message message) {
        try {
            dataOutputStream.write(message.version);
            dataOutputStream.writeShort(message.messageType);
            dataOutputStream.writeInt(message.userId);
            for (char character : message.payload.toCharArray()) {
                dataOutputStream.writeByte((byte) character);
            }
            dataOutputStream.writeByte((byte) DefaultMessageDeserializer.PAYLOAD_TERMINATION_CHAR);
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send message [" + message + "]", e);
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close socket", e);
        }
    }
}
