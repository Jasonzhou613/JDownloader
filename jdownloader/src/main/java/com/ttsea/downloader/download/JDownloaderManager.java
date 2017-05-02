package com.ttsea.downloader.download;

import android.content.Context;
import android.util.Log;

import com.ttsea.downloader.db.DownloadDBHelper;
import com.ttsea.downloader.listener.DownloaderListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 初始化工作，会将数据库里保存了的下载信息加载到downloaderMap
     *
     * @param context 上下文
     */
    private void init(final Context context) {
        downloaderMap = new ConcurrentHashMap<String, Downloader>();

        Observable.just("")
                .subscribeOn(Schedulers.io())
                .retry(2)
                .map(new Function<String, Map<String, Downloader>>() {
                    @Override
                    public Map<String, Downloader> apply(String s) throws Exception {
                        Map<String, Downloader> dMap = new HashMap<String, Downloader>();

                        //获取所有的DownloaderInfo
                        List<DownloaderInfo> infos = DownloadDBHelper.getDownloaderInfos(context, null);
                        for (DownloaderInfo info : infos) {
                            Downloader downloader = new Downloader(info);
                            dMap.put(downloader.getDownloaderInfo().getUrl(), downloader);
                        }

                        return dMap;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Map<String, Downloader>>() {
                            @Override
                            public void accept(Map<String, Downloader> dMap) throws Exception {
                                downloaderMap.clear();
                                downloaderMap.putAll(dMap);
                                JDownloadLog.d(TAG, "init downloaderMap, size:" + downloaderMap.size());
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

    /**
     * 添加一个下载任务
     *
     * @param context  上下文
     * @param url      下载地址，不可为空
     * @param listener 下载监听，可以为空
     */
    public void addNewDownloader(Context context, String url, DownloaderListener listener) {
        DownloaderInfo info = new DownloaderInfo(context.getApplicationContext(), url, listener);
        addNewDownloader(info);
    }

    /**
     * 添加一个下载任务
     *
     * @param info 下载信息，不可为空
     */
    public void addNewDownloader(DownloaderInfo info) {
        addNewDownloader(info, null);
    }

    /**
     * 添加一个下载任务
     *
     * @param info       下载信息，不可为空
     * @param httpOption http相关信息
     */
    public void addNewDownloader(final DownloaderInfo info, HttpOption httpOption) {
        if (downloaderMap.containsKey(info.getUrl())) {
            //如果已经存在了，将info中不是数据库字段的值赋给已存在的Downloader中
            Downloader d = getDownloader(info.getUrl());
            d.getDownloaderInfo().setSaveFileMode(info.getSaveFileMode());
            d.getDownloaderInfo().setReTryCount(info.getReTryCount());
            d.getDownloaderInfo().setExpiredTimeMillis(info.getExpiredTimeMillis());
            d.getDownloaderInfo().setContext(info.getContext());
            d.getDownloaderInfo().setDownloaderListener(info.getDownloaderListener());

            JDownloadLog.d(TAG, "downloader already exist, url:" + info.getUrl());

        } else {
            Downloader downloader = new Downloader(info, httpOption);
            downloaderMap.put(info.getUrl(), downloader);
            JDownloadLog.d(TAG, "add a new downloader url:" + info.getUrl());
            JDownloadLog.d(TAG, "size:" + downloaderMap.size());
        }

        fit();
    }

    /**
     * 自动适配下载任务
     */
    void fit() {
        if (getDownloadingCount() >= downloadPool) {
            return;
        }

        boolean repeat = false;
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null && downloader.getState() == Downloader.STATE_PENDING) {
                downloader.start();
                repeat = true;
                break;
            }
        }
        if (repeat) {
            fit();
        }
    }

    /**
     * 指定开始某个下载任务
     *
     * @param url 下载地址
     */
    public void start(String url) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            if (getDownloadingCount() >= downloadPool) {
                downloader.setState(Downloader.STATE_PENDING);
                downloader.setReason(Downloader.STATE_PENDING);
            } else {
                downloader.start();
            }
        }
    }

    /**
     * 全部开始下载任务<br>
     * 所有被暂停的下载任务，会重新开始下载，其他状态的下载任务不会变更
     */
    public void startAll() {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                if (getDownloadingCount() >= downloadPool) {
                    downloader.setState(Downloader.STATE_PENDING);
                    downloader.setReason(Downloader.STATE_PENDING);
                } else {
                    downloader.start();
                }
            }
        }
        fit();
    }

    /**
     * 暂停指定的下载任务
     *
     * @param url    下载地址
     * @param reason 暂停原因<br>
     *               {@link Downloader#PAUSED_WAITING_FOR_NETWORK},
     *               {@link Downloader#PAUSED_QUEUED_FOR_WIFI},
     *               {@link Downloader#PAUSED_HUMAN},
     *               {@link Downloader#PAUSED_UNKNOWN}
     */
    public void pause(String url, int reason) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            downloader.pause(reason);
            fit();
        }
    }

    /**
     * 暂停所有的下载任务
     *
     * @param reason 暂停原因<br>
     *               {@link Downloader#PAUSED_WAITING_FOR_NETWORK},
     *               {@link Downloader#PAUSED_QUEUED_FOR_WIFI},
     *               {@link Downloader#PAUSED_HUMAN},
     *               {@link Downloader#PAUSED_UNKNOWN}
     */
    public void pauseAll(int reason) {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                downloader.pause(reason);
            }
        }
    }

    /**
     * 取消指定下载任务
     *
     * @param url    下载地址
     * @param reason 取消下载原因<br>
     *               {@link Downloader#ERROR_UNKNOWN},
     *               {@link Downloader#ERROR_UNHANDLED_HTTP_CODE},
     *               {@link Downloader#ERROR_HTTP_DATA_ERROR},
     *               {@link Downloader#ERROR_DEVICE_NOT_FOUND},
     *               {@link Downloader#ERROR_INSUFFICIENT_SPACE},
     *               {@link Downloader#ERROR_FILE_ALREADY_EXISTS},
     *               {@link Downloader#ERROR_HUMAN}
     */
    public void cancel(String url, int reason) {
        Downloader downloader = downloaderMap.get(url);
        if (downloader != null) {
            downloader.cancel(reason);
            fit();
        }
    }

    /**
     * 取消所有下载任务
     *
     * @param reason 取消下载原因<br>
     *               {@link Downloader#ERROR_UNKNOWN},
     *               {@link Downloader#ERROR_UNHANDLED_HTTP_CODE},
     *               {@link Downloader#ERROR_HTTP_DATA_ERROR},
     *               {@link Downloader#ERROR_DEVICE_NOT_FOUND},
     *               {@link Downloader#ERROR_INSUFFICIENT_SPACE},
     *               {@link Downloader#ERROR_FILE_ALREADY_EXISTS},
     *               {@link Downloader#ERROR_HUMAN}
     */
    public void cancelAll(int reason) {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                downloader.cancel(reason);
            }
        }
    }

    /**
     * 获取所有下载器
     *
     * @return Map<String, Downloader>
     */
    public Map<String, Downloader> getDownloaderMap() {
        return downloaderMap;
    }

    /**
     * 根据url获取Downloader
     *
     * @param url 下载地址
     * @return Downloader or null
     */
    public Downloader getDownloader(String url) {
        return downloaderMap.get(url);
    }

    /**
     * 获取正在下载的个数
     *
     * @return 正在下载的个数或者0
     */
    public int getDownloadingCount() {
        int count = 0;
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null && downloader.isRunning()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取同时下载数
     *
     * @return 同时最大的下载数，1-5
     */
    public int getDownloadPool() {
        return downloadPool;
    }

    /**
     * 设置同时最大下载数，至少1个，至多5个，建议设置为2个
     *
     * @param downloadPool 同时最大的下载数
     */
    public void setDownloadPool(int downloadPool) {
        downloadPool = downloadPool < 0 ? 1 : downloadPool;
        downloadPool = downloadPool > 5 ? 5 : downloadPool;

        if (this.downloadPool != downloadPool) {
            this.downloadPool = downloadPool;
            fit();
        }
    }

    /**
     * 删除指定下载任务
     *
     * @param url        下载地址
     * @param deleteFile 是否同时删除下载的文件
     * @return 移除的Downloader or null
     */
    public Downloader delete(String url, boolean deleteFile) {
        Downloader d = downloaderMap.remove(url);
        if (d != null) {
            d.deleteRecord();
            if (deleteFile) {
                d.deleteFile();
            }
        }
        return d;
    }

    /**
     * 删除指定下载任务
     *
     * @param deleteFile 是否同时删除下载的文件
     */
    public void deleteAll(boolean deleteFile) {
        for (Map.Entry<String, Downloader> entry : downloaderMap.entrySet()) {
            Downloader downloader = entry.getValue();
            if (downloader != null) {
                String url = downloader.getDownloaderInfo().getUrl();
                delete(url, deleteFile);
                JDownloadLog.d(TAG, "remove downloader, url:" + url + ", deleteFile:" + deleteFile + ", size:" + downloaderMap.size());
            }
        }
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

    /**
     * 通过key来获取downloader，然后根据时间来排序
     */
    private class ValueComparator implements Comparator<String> {
        private Map<String, Downloader> base;

        //这里需要将要比较的map集合传进来
        ValueComparator(Map<String, Downloader> base) {
            this.base = base;
        }

        //比较的时候，传入的两个参数应该是map的两个key，根据上面传入的要比较的集合base，
        //可以获取到key对应的value，然后按照value进行比较
        public int compare(String o1, String o2) {
            if (base == null) {
                return 0;
            }
            Downloader d1 = base.get(o1);
            Downloader d2 = base.get(o2);
            if (d1 == null || d2 == null) {
                return 0;
            }
            try {
                long d1Time = Long.parseLong(d1.getDownloaderInfo().getAddTimestamp());
                long d2Time = Long.parseLong(d2.getDownloaderInfo().getAddTimestamp());

                return d1Time > d2Time ? 1 : 0;

            } catch (Exception e) {
                e.printStackTrace();
                JDownloadLog.e(TAG, "Exception e:" + e.getMessage());
            }

            return 0;
        }
    }
}