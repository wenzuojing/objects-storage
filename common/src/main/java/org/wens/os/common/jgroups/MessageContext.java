package org.wens.os.common.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * @author wens
 */
public class MessageContext {

    final private Address src;

    final private JChannel channel;

    final private InnerMessage innerMessage;


    protected MessageContext(Address src, JChannel channel, InnerMessage innerMessage) {
        this.src = src;
        this.channel = channel;
        this.innerMessage = innerMessage;
    }


    public void reply(byte[] message) {
        try {
            channel.send(new Message(src, new InnerMessage(innerMessage.getTag(), message)));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    public String getTag() {
        return innerMessage.getTag();
    }


    public byte[] getMessage() {
        return innerMessage.getMessage();
    }


}
