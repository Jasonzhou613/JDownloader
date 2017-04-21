package com.ttsea.downloader.exception;

import com.ttsea.downloader.download.Downloader;

/**
 * 下载异常类 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/17 11:26 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/17 11:26.
 */
public class DownloadException extends Exception {
    private int state = -1;
    private int reason = -1;

    /**
     * 下载异常
     *
     * @param state  see
     *               {@link Downloader#STATE_PENDING},
     *               {@link Downloader#STATE_LINKING},
     *               {@link Downloader#STATE_START},
     *               {@link Downloader#STATE_DOWNLOADING},
     *               {@link Downloader#STATE_PAUSED},
     *               {@link Downloader#STATE_CANCEL},
     *               {@link Downloader#STATE_SUCCESSFUL},
     *               {@link Downloader#STATE_FAILED}
     * @param reason see
     *               {@link Downloader#ERROR_UNKNOWN},
     *               {@link Downloader#ERROR_UNHANDLED_HTTP_CODE},
     *               {@link Downloader#ERROR_HTTP_DATA_ERROR},
     *               {@link Downloader#ERROR_DEVICE_NOT_FOUND},
     *               {@link Downloader#ERROR_INSUFFICIENT_SPACE},
     *               {@link Downloader#ERROR_FILE_ALREADY_EXISTS},
     *               {@link Downloader#ERROR_HUMAN},<br>
     *               {@link Downloader#PAUSED_WAITING_FOR_NETWORK},
     *               {@link Downloader#PAUSED_QUEUED_FOR_WIFI},
     *               {@link Downloader#PAUSED_HUMAN},
     *               {@link Downloader#PAUSED_UNKNOWN}
     */
    public DownloadException(int state, int reason) {
        this(state, reason, "");
    }

    /**
     * 下载异常
     *
     * @param state  see
     *               {@link Downloader#STATE_PENDING},
     *               {@link Downloader#STATE_LINKING},
     *               {@link Downloader#STATE_START},
     *               {@link Downloader#STATE_DOWNLOADING},
     *               {@link Downloader#STATE_PAUSED},
     *               {@link Downloader#STATE_CANCEL},
     *               {@link Downloader#STATE_SUCCESSFUL},
     *               {@link Downloader#STATE_FAILED}
     * @param reason see
     *               {@link Downloader#ERROR_UNKNOWN},
     *               {@link Downloader#ERROR_UNHANDLED_HTTP_CODE},
     *               {@link Downloader#ERROR_HTTP_DATA_ERROR},
     *               {@link Downloader#ERROR_DEVICE_NOT_FOUND},
     *               {@link Downloader#ERROR_INSUFFICIENT_SPACE},
     *               {@link Downloader#ERROR_FILE_ALREADY_EXISTS},
     *               {@link Downloader#ERROR_HUMAN},<br>
     *               {@link Downloader#PAUSED_WAITING_FOR_NETWORK},
     *               {@link Downloader#PAUSED_QUEUED_FOR_WIFI},
     *               {@link Downloader#PAUSED_HUMAN},
     *               {@link Downloader#PAUSED_UNKNOWN}
     * @param msg    错误信息
     */
    public DownloadException(int state, int reason, String msg) {
        super(msg);
        this.state = state;
        this.reason = reason;
    }

    public int getState() {
        return state;
    }

    public int getReason() {
        return reason;
    }
}
