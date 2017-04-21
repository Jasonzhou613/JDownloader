package com.ttsea.downloader.http;

/**
 * Http常量类
 * Created by Jason on 2016/1/6.
 */
public class Http {

    /** Supported request methods. */
    public interface Method {
        String GET = "GET";
        String POST = "POST";
        String PUT = "PUT";
        String DELETE = "DELETE";
        String HEAD = "HEAD";
        String OPTIONS = "OPTIONS";
        String TRACE = "TRACE";
        String PATCH = "PATCH";
    }

    /** 响应求头 域 */
    public static final class ResponseHeadField {
        public static final String X_pad = "X-Pad";
        public static final String X_Android_Sent_Millis = "X-Android-Sent-Millis";
        public static final String X_Android_Selected_Transport = "X-Android-Selected-Transport";
        public static final String X_Android_Response_Source = "X-Android-Response-Source";
        public static final String X_Android_Received_Millis = "X-Android-Received-Millis";
        public static final String Server = "Server";
        public static final String Last_Modified = "Last-Modified";
        public static final String ETag = "ETag";
        public static final String Date = "Date";
        public static final String Content_Type = "Content-Type";
        public static final String Content_Length = "Content-Length";
        public static final String Connection = "Connection";
        public static final String Accept_Ranges = "Accept-Ranges";
    }

    /** 请求头 域 */
    public static final class RequestHeadField {
        public static final String Accept = "Accept";
        public static final String Accept_Encoding = "Accept-Encoding";
        public static final String Referer = "Referer";
        public static final String Charset = "Charset";
        public static final String User_Agent = "User-Agent";
        public static final String Connection = "Connection";
        public static final String Accept_Language = "Accept-Language";
        public static final String Range = "Range";
    }
}
