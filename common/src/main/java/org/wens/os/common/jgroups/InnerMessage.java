package org.wens.os.common.jgroups;

import java.io.Serializable;

/**
 * @author wens
 */
public class InnerMessage implements Serializable {

    private String tag;

    private byte[] message;

    protected InnerMessage(String tag, byte[] message) {
        this.tag = tag;
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }
}
