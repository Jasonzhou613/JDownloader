package com.ttsea.downloader.download;

import android.util.Log;

/**
 * 用来打印想要输出的数据，将DEBUG设为false后会根据{@link #LOG_TAG}来输错日志 <br/>
 * 从高到低为ASSERT, ERROR, WARN, INFO, DEBUG, VERBOSE<br/>
 * 使用adb shell setprop log.tag.{@link #LOG_TAG}来控制输出log等级<br>
 * 默认不输出log
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/1/17 10:26 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/1/17 10:26.
 */
public final class JDownloadLog {

    private static boolean DEBUG = false;
    /**
     * 输出日志等级，当DEBUG为false的时候会根据设置的等级来输出日志<br>
     * 从高到低为ASSERT, ERROR, WARN, INFO, DEBUG, VERBOSE<br>
     */
    private static String LOG_TAG = "jdownloader.log.LEVEL";

    /**
     * 设置是否打印log, 默认为false
     *
     * @param enable true会打印log，false不会打印log
     */
    protected static void enableLog(boolean enable) {
        DEBUG = enable;
    }

    public static void i(String tag, String msg) {
        if (DEBUG || Log.isLoggable(LOG_TAG, Log.INFO)) {
            msg = combineLogMsg(msg);
            Log.i(tag, "" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG || Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            msg = combineLogMsg(msg);
            Log.d(tag, "" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG || Log.isLoggable(LOG_TAG, Log.ERROR)) {
            msg = combineLogMsg(msg);
            Log.e(tag, "" + msg);
        }
    }

    /** 组装动态传参的字符串 将动态参数的字符串拼接成一个字符串 */
    private static String combineLogMsg(String... msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Thread:").append(Thread.currentThread().getId()).append("]");
        sb.append(getCaller()).append(": ");
        if (null != msg) {
            for (String s : msg) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private static String getCaller() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
        String caller = "<unknown>";
        for (int i = 3; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(JDownloadLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);
                caller = callingClass + "." + trace[i].getMethodName()
                        + "(rows:" + trace[i].getLineNumber() + ")";
                break;
            }
        }
        return caller;
    }
}