package com.ttsea.downloader.download;

import android.content.Context;
import android.util.Log;

import com.ttsea.downloader.db.DownloadDBHelper;
import com.ttsea.downloader.listener.DownloaderListener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/13 16:42 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/13 16:42.
 */
public class JDownloaderManager {
    private final String TAG = "JDownloaderManager";

    private volatile static JDownloaderManager instance;

    private final int DEFAULT_DOWNLOAD_POOL = 2;

    private Map<String, Downloader> downloaderMap;
    private int downloadPool = DEFAULT_DOWNLOAD_POOL;

    /** 获取{@link JDownloaderManager} 实例 */
    public static JDownloaderManager getInstance(Context context) {
        if (instance == null) {
            synchronized (JDownloaderManager.class) {
                instance = new JDownloaderManager(context.getApplicationContext());
            }
        }
        return instance;
    }

    private JDownloaderManager(Context appContext) {
        init(appContext);
    }

    private void init(final Context context) {
        downloaderMap = new TreeMap<String, Downloader>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (downloaderMap == null) {
                    return 0;
                }
                Downloader d1 = downloaderMap.get(o1);
                Downloader d2 = downloaderMap.get(o2);
                if (d1 == null || d2 == null) {
                    return 0;
                }
                try {
                    long d1Time = Long.parseLong(d1.getDownloaderInfo().getAddTimestamp());
                    long d2Time = Long.parseLong(d2.getDownloaderInfo().getAddTimestamp());

                    return d1Time > d2Time ? 1 : 0;

                } catch (Exception e) {
                    JDownloadLog.e(TAG, "Exception e:" + e.getMessage());
                }

                return 0;
            }
        });

        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Map<String, Downloader>>() {
                    @Override
                    public Map<String, Downloader> apply(String s) throws Exception {
                        //获取所有的DownloaderInfo
                        List<DownloaderInfo> infos = DownloadDBHelper.getDownloaderInfos(context, null);
                        for (DownloaderInfo info : infos) {
                            Downloader downloader = new Downloader(info);
                        }

                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Map<String, Downloader>>() {
                            @Override
                            public void accept(Map<String, Downloader> dMap) throws Exception {
                                downloaderMap.clear();
                                downloaderMap.putAll(dMap);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                JDownloadLog.e(TAG, "Throwable e:" + throwable.getMessage());
                                downloaderMap.clear();
                            }
                        });
    }

    public void addDownloader(Context context, String url, DownloaderListener listener) {
        DownloaderInfo info = new DownloaderInfo(context.getApplicationContext(), url, listener);
        addDownloader(info);
    }

    public void addDownloader(DownloaderInfo info) {
        addDownloader(info, null);
    }

    public void addDownloader(DownloaderInfo info, HttpOption httpOption) {
        Downloader downloader = new Downloader(info, httpOption);
        if (downloaderMap.containsKey(info.getUrl())) {
            JDownloadLog.d(TAG, "downloader already exist, url:" + info.getUrl());
            return;
        }

        downloaderMap.put(info.getUrl(), downloader);
    }


    public void start(String url) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            downloader.start();
        }
    }

    public void startAll() {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                downloader.start();
            }
        }
    }

    public void pause(String url, int reason) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            downloader.pause(reason);
        }
    }

    public void pauseAll(int reason) {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                downloader.pause(reason);
            }
        }
    }

    public void cancel(String url, int reason) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            downloader.cancel(reason);
        }
    }

    public void cancelAll(int reason) {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                downloader.cancel(reason);
            }
        }
    }

    public Map<String, Downloader> getDownloaderMap() {
        return downloaderMap;
    }

    public int getDownloadPool() {
        return downloadPool;
    }

    public void setDownloadPool(int downloadPool) {
        this.downloadPool = downloadPool;
    }

    /**
     * 设置是否为调试模式，调试模式下会打印log<br>
     * 默认为false
     *
     * @param debug true为调试模式，false不是调试模式
     */
    public void debugMode(boolean debug) {
        JDownloadLog.enableLog(debug);

        Log.d(TAG, "JDownloader debug:" + debug);
    }
}