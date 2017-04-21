package com.ttsea.downloader.sample.download;


import com.ttsea.downloader.sample.DigitUtils;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/21 21:26 <br>
 * <b>author:</b> Administrator <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/21 21:26.
 */
public class Utils {

    public static String getSpeedWithUnit(long speed) {
        speed = speed > 0 ? speed : 0;
        float size = speed;
        float radices = 1024.0f;
        if (speed < 1024) {
            return size + "b/s";
        }

        size = DigitUtils.getFloat((speed / radices), 2);
        if (size < 1024) {
            return size + "kb/s";
        }

        size = DigitUtils.getFloat((speed / radices / radices), 2);
        return size + "M/s";
    }

    public static String getFileSizeWithUnit(long length) {
        length = length > 0 ? length : 0;
        float size = length;
        float radices = 1024.0f;
        if (length < 1024) {
            return size + "b";
        }

        size = DigitUtils.getFloat((length / radices), 2);
        if (size < 1024) {
            return size + "kb";
        }

        size = DigitUtils.getFloat((length / radices / radices), 2);
        return size + "M";
    }

    public static boolean isEmpty(String str) {
        if (str == null || str.length() < 1) {
            return true;
        }
        return false;
    }
}
