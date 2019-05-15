package org.wens.os.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wens
 */
public class MessageDisgestUtils {

    public static MessageDigest sha256(){
        try {
            return MessageDigest.getInstance("sha-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
