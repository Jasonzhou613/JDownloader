package com.ttsea.downloader.listener;

import com.ttsea.downloader.download.Downloader;

/**
 * 下载器监听，所有定义的方法都是运行在主线程中 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 17:02 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 17:02.
 */
public interface DownloaderListener extends DownloadingProgressListener {

    /** 正在等待下载 */
    void onPending();

    /** 正在链接... */
    void onLinking();

    /** 开始下载 */
    void onStart();

    /**
     * 暂停下载
     *
     * @param reason 暂停下载原因，
     *               see
     *               {@link Downloader#PAUSED_WAITING_FOR_NETWORK},
     *               {@link Downloader#PAUSED_QUEUED_FOR_WIFI},
     *               {@link Downloader#PAUSED_HUMAN},
     *               {@link Downloader#PAUSED_UNKNOWN}
     */
    void onPause(int reason);

    /**
     * 取消下载
     *
     * @param reason 取消下载原因，
     *               see
     *               {@link Downloader#ERROR_UNKNOWN},
     *               {@link Downloader#ERROR_UNHANDLED_HTTP_CODE},
     *               {@link Downloader#ERROR_HTTP_DATA_ERROR},
     *               {@link Downloader#ERROR_DEVICE_NOT_FOUND},
     *               {@link Downloader#ERROR_INSUFFICIENT_SPACE},
     *               {@link Downloader#ERROR_FILE_ALREADY_EXISTS},
     *               {@link Downloader#ERROR_HUMAN}
     */
    void onCancel(int reason);

    /** 完成下载 */
    void onComplete();

    /** 下载出错 */
    void onError(Throwable e);
}
