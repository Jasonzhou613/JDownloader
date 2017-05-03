package com.ttsea.downloader.download;


import com.ttsea.downloader.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/10 14:23 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/10 14:23.
 */
final class RetrofitClient {
    private final static String TAG = "RetrofitClient";
    /** Http请求全局变量 */
    private static OkHttpClient okHttpClient;

    private static OkHttpClient getHttpClient(HttpOption httpOption) {
        if (okHttpClient == null) {
            synchronized (RetrofitClient.class) {
                okHttpClient = new OkHttpClient.Builder()
                        //.cache(cache)
                        .connectTimeout(httpOption.getConnectionTimeOut(), TimeUnit.MILLISECONDS)
                        .writeTimeout(httpOption.getWriteTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(httpOption.getReadTimeout(), TimeUnit.MILLISECONDS)
                        .connectionPool(httpOption.getConnectionPool())
                        .build();

                JDownloadLog.d(TAG, "connectTimeoutMillis: " + okHttpClient.connectTimeoutMillis() +
                        ", \n" + "writeTimeoutMillis: " + okHttpClient.writeTimeoutMillis() +
                        ", \n" + "readTimeoutMillis: " + okHttpClient.readTimeoutMillis() +
                        ", \n" + "connectionPoolCount: " + okHttpClient.connectionPool().connectionCount() +
                        ", \n" + "connectionPoolIdleCount: " + okHttpClient.connectionPool().idleConnectionCount() +
                        "");
            }
        }

        return okHttpClient;
    }

    /**
     * 获取Retrofit实例，这个方法与{@link #getRetrofit(HttpOption)}的区别是，这里会采用一个新的OkHttpClient作为请求实例
     *
     * @param httpOption http选项
     * @return Retrofit
     */
    public static Retrofit getNewRetrofit(HttpOption httpOption) {
        return new Retrofit.Builder()
                .baseUrl("http://www.ttsea.com/")
                .client(getHttpClient(httpOption).newBuilder().build())
                .build();
    }

    /**
     * 获取Retrofit实例
     *
     * @param httpOption http选项
     * @return Retrofit
     */
    public static Retrofit getRetrofit(HttpOption httpOption) {
        return new Retrofit.Builder()
                .baseUrl("http://www.ttsea.com/")
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getHttpClient(httpOption))
                .build();
    }
}
