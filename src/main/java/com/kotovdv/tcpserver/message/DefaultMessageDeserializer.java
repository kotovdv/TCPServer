package com.kotovdv.tcpserver.message;

import com.kotovdv.tcpserver.core.message.MessageDeserializer;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Optional;

/**
 * Deserializer for the following message structure.
 * <p>
 * - Version (1 byte)
 * <p>
 * - Message Type (2 byte integer)
 * <p>
 * - User ID (4 byte integer)
 * <p>
 * - Payload (variable length ASCII string)
 */
public class DefaultMessageDeserializer implements MessageDeserializer<Message> {

    public static final char PAYLOAD_TERMINATION_CHAR = '\0';

    @Override
    public Optional<Message> readNext(DataInputStream stream) throws IOException {
        return read(stream);
    }

    private Optional<Message> read(DataInputStream in) throws IOException {
        boolean readStarted = false;

        try {
            byte version = in.readByte();
            readStarted = true;
            short messageType = in.readShort();
            int userId = in.readInt();
            String payload = readPayload(in);

            return Optional.of(new Message(
                    version,
                    messageType,
                    userId,
                    payload
            ));
        } catch (EOFException e) {
            if (readStarted) {
                //If stream/socket was closed prematurely -> rethrow exception.
                throw new IOException("InputStream ended prematurely", e);
            }
            //Otherwise stream was closed after N complete messages -> do nothing.
            return Optional.empty();
        }
    }

    private String readPayload(DataInputStream in) throws IOException {
        StringBuilder payload = new StringBuilder();
        char currentChar;

        while ((currentChar = (char) in.readByte()) != PAYLOAD_TERMINATION_CHAR) {
            payload.append(currentChar);
        }

        return payload.toString();
    }
}
