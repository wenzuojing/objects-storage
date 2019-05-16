package org.wens.os.common.queue;


import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedisMessageQueueTest {

    @Test
    public void test_send_1() throws InterruptedException {

        JedisPool jedisPool = new JedisPool("192.168.15.101", 6379);

        MessageQueue messageQueue = new RedisMessageQueue("test", jedisPool);

        messageQueue.start();

        List<byte[]> messages = new CopyOnWriteArrayList<>();

        messageQueue.addMessageListener(data -> {
            messages.add(data);
        });

        messageQueue.addMessageListener(data -> {
            messages.add(data);
        });

        int n = 0;

        for (int i = 0; i < n; i++) {
            messageQueue.send(("hi" + i).getBytes());
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        Assert.assertEquals(2 * n, messages.size());

    }

    @Test
    public void test_send_2() throws InterruptedException {

        JedisPool jedisPool = new JedisPool("192.168.15.101", 6379);

        MessageQueue messageQueue = new RedisMessageQueue("test", jedisPool);
        messageQueue.start();
        List<byte[]> messages = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            MessageQueue messageQueue2 = new RedisMessageQueue("test", jedisPool);
            messageQueue2.start();
            messageQueue2.addMessageListener(data -> {
                messages.add(data);
            });
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();
        messageQueue.addMessageListener(data -> {
            messages.add(data);
        });

        int n = 0;
        for (int i = 0; i < n; i++) {
            messageQueue.send(("hi" + i).getBytes());
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        Assert.assertEquals(2 * n, messages.size());


    }

    @Test
    public void test_close() throws InterruptedException {

        JedisPool jedisPool = new JedisPool("192.168.15.101", 6379);
        MessageQueue messageQueue = new RedisMessageQueue("test", jedisPool);
        messageQueue.start();
        List<byte[]> messages = new CopyOnWriteArrayList<>();

        messageQueue.addMessageListener(data -> {
            messages.add(data);
        });

        int n = 0;
        for (int i = 0; i < n; i++) {
            messageQueue.send(("hi" + i).getBytes());
        }

        messageQueue.close();

        for (int i = 0; i < n; i++) {
            messageQueue.send(("hi" + i).getBytes());
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        Assert.assertEquals(n, messages.size());


    }
}
