package com.kotovdv.tcpserver.message;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PrintingMessageHandlerTest {

    /**
     * Check message is printed in the expected format with exact values.
     */
    @Test
    public void checkPrinting() {
        Message message = new Message(
                (byte) 15,
                (short) 2342,
                3453456,
                "Hello world"
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintingMessageHandler handler = new PrintingMessageHandler(outputStream);
        handler.handle(message);

        String printedMessage = new String(outputStream.toByteArray(), StandardCharsets.US_ASCII);
        String[] parts = printedMessage.split(System.lineSeparator());

        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(parts).hasSize(4);
            assertions.assertThat(parts[0]).isEqualTo("Version: " + message.version);
            assertions.assertThat(parts[1]).isEqualTo("Message Type: " + message.messageType);
            assertions.assertThat(parts[2]).isEqualTo("User ID: " + message.userId);
            assertions.assertThat(parts[3]).isEqualTo("Payload: " + message.payload);
        });
    }
}
