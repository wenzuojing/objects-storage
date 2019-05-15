package org.wens.os.common.util;




import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.wens.os.common.io.CalDigestInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * @author wens
 */
public class IO {

    public static InputStream textToInputStream(String text){
        return new ByteArrayInputStream(text.getBytes(Charset.forName("utf-8")));
    }


    public static void main(String[] args) throws IOException {
        MessageDigest messageDigest = MessageDisgestUtils.sha256();
        InputStream  inputStream = new CalDigestInputStream(new FileInputStream("/Users/wens/Downloads/Deeplearning深度学习笔记v5.6.docx") ,  messageDigest );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        byte[] b  = new byte[2];
        int n  = -1 ;
        while (( n = inputStream.read() ) != -1 ){
        }

        System.out.println(Base64.encodeBase64String(messageDigest.digest()));


    }

}
