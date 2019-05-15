package org.wens.os.common.queue;

/**
 * @author wens
 */
public interface MessageQueue {


    void send(byte[] data);

    void consume(MessageListener messageListener);


    void start();

    void close();
}
