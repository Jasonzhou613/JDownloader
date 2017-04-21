package com.ttsea.downloader.download;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/10 13:54 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/10 13:54.
 */
interface TaskHandler {

    /** 开始下载 */
    void start();

    /**
     * 暂停
     *
     * @param reason 暂停原因
     */
    void pause(int reason);

    /** 唤醒 */
    void resume();

    /**
     * 取消
     *
     * @param reason 取消原因
     */
    void cancel(int reason);

    /** 重新下载 */
    void reDownload();

    /** 是否已暂停 */
    boolean isPaused();

    /** 是否已取消 */
    boolean isCancelled();
}
