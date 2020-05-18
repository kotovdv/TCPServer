package com.kotovdv.tcpserver.util;

import com.kotovdv.tcpserver.core.message.MessageHandler;
import com.kotovdv.tcpserver.message.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CountingMessageHandler implements MessageHandler<Message> {

    public final CountDownLatch latch;
    public final List<Message> storage = Collections.synchronizedList(new ArrayList<>());

    public CountingMessageHandler(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void handle(Message message) {
        storage.add(message);
        latch.countDown();
    }
}
