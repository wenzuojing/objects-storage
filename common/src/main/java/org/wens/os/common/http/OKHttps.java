package org.wens.os.common.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * @author wens
 */
public class OKHttps {

    private final static Logger log = LoggerFactory.getLogger(OKHttps.class);

    private final static OkHttpClient okHttpClient ;

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor((message) -> log.info(message));
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        okHttpClient = new OkHttpClient.Builder().addInterceptor(logging).connectTimeout(Duration.ofSeconds(6)).readTimeout(Duration.ofMinutes(5)).callTimeout(Duration.ofMinutes(5)).build();
    }


    public static Response post(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response put(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder().url(url).put(requestBody).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response delete(String url) throws IOException {
        Request request = new Request.Builder().url(url).delete().build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return okHttpClient.newCall(request).execute();
    }

}
