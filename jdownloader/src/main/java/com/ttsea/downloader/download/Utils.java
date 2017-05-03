package com.ttsea.downloader.download;

import android.content.Context;

import com.ttsea.downloader.R;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * //To do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2016/5/5 11:14 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2016/5/5 11:14
 */
class Utils {
    private final static String TAG = "Utils";

    /**
     * 对map排序
     *
     * @param map 要排序的map
     * @return LinkedHashMap
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

    /** 判断str是否为空 */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() < 1) {
            return true;
        }
        return false;
    }

    /**
     * 获取默认下载的根目录
     *
     * @param context 上下文
     * @return String
     */
    public static String getDefaultSavePath(Context context) {
        String saveDir = SdStatusUtils.getExternalStorageAbsoluteDir();

        String rootDir = getStringById(context, R.string._j_download_root_dir);
        String lowerDir = getStringById(context, R.string._j_download_lower_dir);

        if (!Utils.isEmpty(rootDir) && !"null".equals(rootDir)) {
            saveDir = saveDir + File.separator + rootDir;

            if (!Utils.isEmpty(lowerDir) && !"null".equals(lowerDir)) {
                saveDir = saveDir + File.separator + lowerDir;
            }
        }
        createDirIfNeed(saveDir);

        return saveDir;
    }

    /**
     * 如果该目录不存在，则创建
     *
     * @param dirPath 目录路径
     * @return 创建成功:true，创建失败:false
     */
    private static boolean createDirIfNeed(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                JDownloadLog.d(TAG, "createDirIfNeed, create success, dir:" + dirPath);
                return true;
            } else {
                JDownloadLog.d(TAG, "createDirIfNeed, create failed, dir:" + dirPath);
            }
            return false;
        }
        return true;
    }

    private static String getStringById(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    public static String getFileName(String url) {
        String name = url.substring(url.lastIndexOf("/") + 1);
        if (Utils.isEmpty(name)) {
            name = "unknown_name";
        }
        return name;
    }
}
