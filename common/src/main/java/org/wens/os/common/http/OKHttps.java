package org.wens.os.common.http;

import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

/**
 * @author wens
 */
public class OKHttps {

    private final static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(6)).readTimeout(Duration.ofSeconds(6)).callTimeout(Duration.ofHours(1)).build() ;


    public static Response post(String url, RequestBody requestBody ) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response put(String url, RequestBody requestBody ) throws IOException {
        Request request = new Request.Builder().url(url).put(requestBody).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response delete(String url ) throws IOException {
        Request request = new Request.Builder().url(url).delete().build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return okHttpClient.newCall(request).execute();
    }

}
