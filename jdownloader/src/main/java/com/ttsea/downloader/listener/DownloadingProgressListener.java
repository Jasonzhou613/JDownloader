package com.ttsea.downloader.listener;

/**
 * 下载进度监听 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 10:28 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 10:28.
 */
public interface DownloadingProgressListener {

    /**
     * 正在下载...
     *
     * @param hasReadLength   已经下载了的长度
     * @param needReadLength  总共需要读取的长度
     * @param speedBytePerSec 下载速度，byte/s
     */
    void onDownloading(long hasReadLength, long needReadLength, long speedBytePerSec);
}
