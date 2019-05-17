package org.wens.os.common.jgroups;

/**
 * @author wens
 */
public interface MessageListener {

    void onMessage(MessageContext context);

}
