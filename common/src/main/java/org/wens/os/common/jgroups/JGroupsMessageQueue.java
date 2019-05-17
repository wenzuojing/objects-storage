package org.wens.os.common.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wens
 */
public class JGroupsMessageQueue extends ReceiverAdapter {

    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final static Logger log = LoggerFactory.getLogger(JGroupsMessageQueue.class);

    private JChannel channel;

    private List<MessageListener> messageListeners;

    public JGroupsMessageQueue(String name) {

        try {
            this.channel = new JChannel();
            this.channel.setReceiver(this);
            this.channel.connect(name);
            this.messageListeners = new CopyOnWriteArrayList<>();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void send(byte[] data, String tag) {
        Message message = new Message(null, new InnerMessage(tag, data));
        try {
            channel.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void removeMessageListener(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    @Override
    public void receive(Message msg) {
        if (msg.getSrc().equals(channel.getAddress())) {
            return;
        }
        MessageContext messageContext = new MessageContext(msg.getSrc(), channel, msg.getObject());
        executorService.submit(() -> {
            messageListeners.forEach(ml -> {
                try {

                    ml.onMessage(messageContext);
                } catch (Throwable t) {
                    log.error("handle message fail.", t);
                }

            });
        });
    }

}
