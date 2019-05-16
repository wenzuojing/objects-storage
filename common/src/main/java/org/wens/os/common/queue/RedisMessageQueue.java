package org.wens.os.common.queue;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wens.os.common.OSException;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author wens
 */
public class RedisMessageQueue extends BinaryJedisPubSub implements MessageQueue {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private byte[] topic;

    private JedisPool jedisPool;

    private volatile boolean running = false;

    private List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();


    public RedisMessageQueue(String topic, JedisPool jedisPool) {
        this.topic = topic.getBytes(Charset.forName("utf-8"));
        this.jedisPool = jedisPool;
    }

    @Override
    public void send(byte[] data) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(topic, data);
        }
    }

    @Override
    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        messageListeners.forEach(messageListener -> {
            try {
                messageListener.onMessage(message);
            } catch (Exception e) {
                log.error("Invoke onMessage fail.", e);
            }
        });
    }

    @Override
    public void start() {
        synchronized (this) {
            if (!running) {
                running = true;
                CountDownLatch countDownLatch = new CountDownLatch(1);
                new Thread(() -> {
                    while (running) {
                        try (Jedis jedis = jedisPool.getResource()) {
                            countDownLatch.countDown();
                            jedis.subscribe(RedisMessageQueue.this, topic);
                        } catch (Throwable t) {
                            log.error("subscribe message fail.", t);
                        }
                    }
                }, String.format("subscribe-%s", new String(topic, Charset.forName("utf-8")))).start();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    //
                }
            }
        }
    }

    @Override
    public void close() {
        running = false;
        messageListeners.clear();
        try {
            unsubscribe();
        } catch (Throwable t) {
        }


    }
}
