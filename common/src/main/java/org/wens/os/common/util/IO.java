package org.wens.os.common.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author wens
 */
public class IO {

    public static InputStream textToInputStream(String text) {
        return new ByteArrayInputStream(text.getBytes(Charset.forName("utf-8")));
    }


}
