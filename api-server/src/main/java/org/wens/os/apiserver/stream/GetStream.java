package org.wens.os.apiserver.stream;

import okhttp3.Response;
import org.wens.os.common.http.OKHttps;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wens
 */
public class GetStream implements Closeable {

    private String server ;

    private String name ;

    private Closeable closeable ;

    public GetStream(String server , String name ){
        this.server = server ;
        this.name = name ;
    }

    public InputStream read() throws IOException {
        Response response = OKHttps.get(String.format("http://%s/objects/%s", server, name));
        if(response.code() == 404 ){
            return null ;
        }
        if(response.code() == 200 ){
            throw new IOException("read fail");
        }
        closeable = response ;
        return response.body().byteStream();
    }

    @Override
    public void close() throws IOException {
        if(closeable != null ){
            closeable.close();
        }
    }
}
