package com.kotovdv.tcpserver;

import com.kotovdv.tcpserver.core.server.Server;
import com.kotovdv.tcpserver.core.server.SocketConnectionHandler;
import com.kotovdv.tcpserver.message.DefaultMessageDeserializer;
import com.kotovdv.tcpserver.message.Message;
import com.kotovdv.tcpserver.util.CountingMessageHandler;
import com.kotovdv.tcpserver.util.TCPServerClient;
import com.kotovdv.tcpserver.util.TCPUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ServerTest {

    private final DefaultMessageDeserializer deserializer = new DefaultMessageDeserializer();

    /**
     * Checking, that all the messages from single client are going to be received as is and in proper order by the server.
     */
    @Test
    public void checkCommunicationWithSingleClient() throws InterruptedException {
        int port = TCPUtil.findFreePort();
        int messagesAmount = ThreadLocalRandom.current().nextInt(5, 10);

        CountingMessageHandler handler = new CountingMessageHandler(new CountDownLatch(messagesAmount));
        Server server = new Server(port, 1, new SocketConnectionHandler<>(
                handler,
                deserializer
        ));
        server.start();

        TCPServerClient client = TCPServerClient.create(port);

        List<Message> messages = generateMessages(messagesAmount);
        for (Message message : messages) {
            client.sendMessage(message);
        }

        handler.latch.await();
        client.close();

        server.stop(100, TimeUnit.MILLISECONDS);

        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(handler.storage).hasSize(messagesAmount);
            for (int i = 0; i < handler.storage.size(); i++) {
                Message expected = messages.get(i);
                Message actual = handler.storage.get(i);
                assertions.assertThat(actual).isEqualToComparingFieldByField(expected);
            }
        });
    }


    private List<Message> generateMessages(int amount) {
        List<Message> messages = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < amount; i++) {
            Message message = new Message(
                    (byte) random.nextInt(-128, 127),
                    (short) random.nextInt(-500, 500),
                    random.nextInt(),
                    String.valueOf(random.nextInt())
            );
            messages.add(message);
        }

        return messages;
    }
}
