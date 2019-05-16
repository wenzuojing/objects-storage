package org.wens.os.common.queue;

/**
 * @author wens
 */
public interface MessageQueue {


    void send(byte[] data);

    void addMessageListener(MessageListener messageListener);

    void start();

    void close();
}
