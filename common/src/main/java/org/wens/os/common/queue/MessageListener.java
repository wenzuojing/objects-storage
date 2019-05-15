package org.wens.os.common.queue;

/**
 * @author wens
 *
 */
public interface MessageListener {

    void onMessage(byte[] data);

}
