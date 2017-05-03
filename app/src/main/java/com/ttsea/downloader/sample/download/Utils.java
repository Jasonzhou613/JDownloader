package com.ttsea.downloader.sample.download;


import com.ttsea.downloader.download.Downloader;
import com.ttsea.downloader.sample.DigitUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/21 21:26 <br>
 * <b>author:</b> Administrator <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/21 21:26.
 */
class Utils {

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

    /**
     * 对map排序
     *
     * @param map
     * @return
     */
    public static Map<String, Downloader> sortByValue(Map<String, Downloader> map) {
        List<Map.Entry<String, Downloader>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Downloader>>() {
            @Override
            public int compare(Map.Entry<String, Downloader> o1, Map.Entry<String, Downloader> o2) {
                Downloader d1 = o1.getValue();
                Downloader d2 = o2.getValue();
                if (d1 == null || d2 == null) {
                    return 0;
                }
                try {
                    long d1Time = Long.parseLong(d1.getDownloaderInfo().getAddTimestamp());
                    long d2Time = Long.parseLong(d2.getDownloaderInfo().getAddTimestamp());

                    return (int) (d1Time - d2Time);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        Map<String, Downloader> result = new LinkedHashMap<String, Downloader>();
        for (Map.Entry<String, Downloader> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
