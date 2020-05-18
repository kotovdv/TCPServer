package com.kotovdv.tcpserver.message;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class DefaultMessageDeserializerTest {

    private final DefaultMessageDeserializer deserializer = new DefaultMessageDeserializer();

    /**
     * In case if end of stream was met before any data was read,
     * we expect to receive  Optional.empty.
     */
    @Test
    public void emptyStream() throws IOException {
        DataInputStream stream = createInputStream(new byte[0]);
        Optional<Message> actual = deserializer.readNext(stream);

        assertThat(actual).isEmpty();
    }

    /**
     * In case if end of the stream is met only after 1 field was sent
     * we expect to get IOException.
     */
    @Test(expected = IOException.class)
    public void streamWithSingleField() throws IOException {
        DataInputStream stream = createInputStream(new byte[]{
                10, //Version == 10
        });
        deserializer.readNext(stream);
    }

    /**
     * In case if end of the stream is met only after 2 fields were sent
     * we expect to get IOException.
     */
    @Test(expected = IOException.class)
    public void streamWithTwoFields() throws IOException {
        DataInputStream stream = createInputStream(new byte[]{
                10, //Version == 10
                0, 127, //Message Type == 127
        });
        deserializer.readNext(stream);
    }


    /**
     * In case if end of the stream is met only after 3 fields were sent
     * we expect to get IOException.
     */
    @Test(expected = IOException.class)
    public void streamWithThreeFields() throws IOException {
        DataInputStream stream = createInputStream(new byte[]{
                10, //Version == 10
                0, 127, //Message Type == 127
                0, 0, 1, 0, //User ID == 256
        });
        deserializer.readNext(stream);
    }


    /**
     * In case if end of the stream is met only after 4 fields were sent, but there is no termination char at the end
     * we expect to get IOException.
     */
    @Test(expected = IOException.class)
    public void streamWithFourFieldsNoTermination() throws IOException {
        DataInputStream stream = createInputStream(new byte[]{
                10, //Version == 10
                0, 127, //Message Type == 127
                0, 0, 1, 0, //User ID == 256
                72, 69, 76, 76, 79, //Payload == HELLO
        });
        deserializer.readNext(stream);
    }

    /**
     * End of the stream is met right after complete message and termination symbol,
     * we expect to receive fully formed message wrapped in Optional.
     * Subsequent calls to deserializer with the same stream should result in Optional.empty being returned.
     */
    @Test
    public void streamWithFourFieldsWithTermination() throws IOException {
        DataInputStream stream = createInputStream(new byte[]{
                10, //Version == 10
                0, 127, //Message Type == 127
                0, 0, 1, 0, //User ID == 256
                72, 69, 76, 76, 79, //Payload == HELLO
                DefaultMessageDeserializer.PAYLOAD_TERMINATION_CHAR
        });

        Optional<Message> actual = deserializer.readNext(stream);
        assertSoftly(assertions -> {
            assertions.assertThat(actual).isPresent();
            actual.ifPresent(message -> {
                assertions.assertThat(message.version).isEqualTo((byte) 10);
                assertions.assertThat(message.messageType).isEqualTo((short) 127);
                assertions.assertThat(message.userId).isEqualTo(256);
                assertions.assertThat(message.payload).isEqualTo("HELLO");
            });
        });
    }

    /**
     * Checking if it is possible to read multiple messages separated by termination character.
     */
    @Test
    public void multipleSuccessfulReads() throws IOException {
        int messagesCount = ThreadLocalRandom.current().nextInt(5, 20);
        byte[] bytes = createMessages(messagesCount);
        DataInputStream stream = createInputStream(bytes);

        try (AutoCloseableSoftAssertions assertions = new AutoCloseableSoftAssertions()) {
            Optional<Message> current;
            int actualAmount = 0;
            while ((current = deserializer.readNext(stream)).isPresent()) {
                actualAmount++;
                Message message = current.get();
                assertions.assertThat(message.version).isEqualTo((byte) actualAmount);
                assertions.assertThat(message.messageType).isEqualTo((short) actualAmount);
                assertions.assertThat(message.userId).isEqualTo(actualAmount);
                assertions.assertThat(message.payload).hasSize(1);
                assertions.assertThat(message.payload.charAt(0)).isEqualTo((char) actualAmount);
            }

            assertions.assertThat(actualAmount).isEqualTo(messagesCount);
        }
    }

    /**
     * Check that after all messages are read all subsequent calls return Optional.empty.
     */
    @Test
    public void allReadsReturnEmptyAfterStreamEnded() throws IOException {
        DataInputStream stream = createInputStream(createMessages(
                ThreadLocalRandom.current().nextInt(3, 6))
        );
        //Read all existing messages.
        while (deserializer.readNext(stream).isPresent()) {
        }

        int amountOfCalls = ThreadLocalRandom.current().nextInt(10, 30);
        try (AutoCloseableSoftAssertions assertions = new AutoCloseableSoftAssertions()) {
            for (int i = 0; i < amountOfCalls; i++) {
                Optional<Message> result = deserializer.readNext(stream);
                assertions.assertThat(result).as("Check call %d resulted in empty message", i + 1).isEmpty();
            }
        }
    }

    private DataInputStream createInputStream(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        return new DataInputStream(new BufferedInputStream(byteArrayInputStream));
    }

    private byte[] createMessages(int amount) {
        int messageSize = 9;
        byte[] bytes = new byte[messageSize * amount];

        for (int i = 0; i < amount; i++) {
            byte identifier = (byte) (i + 1);
            //Version
            bytes[messageSize * i] = identifier;
            //Message Type
            bytes[messageSize * i + 1] = 0;
            bytes[messageSize * i + 2] = identifier;
            //User ID
            bytes[messageSize * i + 3] = 0;
            bytes[messageSize * i + 4] = 0;
            bytes[messageSize * i + 5] = 0;
            bytes[messageSize * i + 6] = identifier;
            //Payload
            bytes[messageSize * i + 7] = identifier;
            //Termination
            bytes[messageSize * i + 8] = DefaultMessageDeserializer.PAYLOAD_TERMINATION_CHAR;
        }

        return bytes;
    }
}
