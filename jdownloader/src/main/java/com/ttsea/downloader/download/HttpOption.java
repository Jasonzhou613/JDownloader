package com.ttsea.downloader.download;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;

/**
 * 下载信息选项，启动Downloader时，可以通过这个类来配置相关信息 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2016/1/6 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2016/5/5 10:05
 */
public class HttpOption {
    private Builder builder;

    public Builder getBuilder() {
        return builder;
    }

    private HttpOption(Builder builder) {
        this.builder = builder;
    }

    /** 返回链接时间，单位毫秒 */
    public long getConnectionTimeOut() {
        return builder.connectionTimeOut;
    }

    /** 返回写入时间，单位毫秒 */
    public long getWriteTimeout() {
        return builder.writeTimeout;
    }

    /** 返回读取时间，单位毫秒 */
    public long getReadTimeout() {
        return builder.readTimeout;
    }

    /** 返回ConnectionPool连接池 */
    public ConnectionPool getConnectionPool() {
        return builder.connectionPool;
    }

    @Override
    public String toString() {
        return "HttpOption{" +
                "builder=" + builder.toString() +
                '}';
    }

    public static class Builder {
        private final static int DEFAULT_CONNECTION_TIME_OUT = 15 * 1000;

        //Http request
        private long connectionTimeOut = -1;
        private long writeTimeout = -1;
        private long readTimeout = -1;
        private ConnectionPool connectionPool = null;

        public Builder() {
        }

        public HttpOption build() {
            if (connectionTimeOut < 0) {
                connectionTimeOut = DEFAULT_CONNECTION_TIME_OUT;
            }

            if (writeTimeout < 0) {
                writeTimeout = DEFAULT_CONNECTION_TIME_OUT;
            }

            if (readTimeout < 0) {
                readTimeout = DEFAULT_CONNECTION_TIME_OUT;
            }

            if (connectionPool == null) {
                connectionPool = new ConnectionPool(6, 6, TimeUnit.MINUTES);
            }

            return new HttpOption(this);
        }

        /** 链接时间，单位毫秒 */
        public void setConnectionTimeOut(int connectionTimeOut) {
            this.connectionTimeOut = connectionTimeOut;
        }

        /** 写入时间，单位毫秒 */
        public void setWriteTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        /** 读取时间，单位毫秒 */
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        /** 设置ConnectionPool连接池 */
        public void setConnectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "connectionTimeOut=" + connectionTimeOut +
                    ", writeTimeout=" + writeTimeout +
                    ", readTimeout=" + readTimeout +
                    ", connectionPool=" + connectionPool +
                    '}';
        }
    }
}
