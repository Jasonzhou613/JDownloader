package com.ttsea.downloader.download;


import android.content.Context;

import com.ttsea.downloader.listener.DownloaderListener;


/**
 * 下载器相关信息 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 16:47 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 16:47.
 */
public class DownloaderInfo {
    private final int DEFAULT_RETRY_COUNT = 2;

    /** 1.每个线程的id */
    private String threadId;
    /** 2.下载地址 */
    private String url;
    /** 3.本地存储路径 */
    private String saveFilePath;
    /** 4.本地存储的文件名 */
    private String fileName;
    /** 5.下载信息描述，可以用来显示在通知栏里 */
    private String description;
    /** 6.添加时的时间 */
    private String addTimestamp;
    /** 7.最后修改时间 */
    private String lastModifiedTimestamp;
    /** 8.下载文件的类型 */
    private String mediaType;
    /** 9.提供下载失败原因 */
    private int reason;
    /** 10.当前下载的状态 */
    private int state;
    /** 11.文件总长度 */
    private long contentLength;
    /** 12.开始下载的点 */
    private long startBytes;
    /** 13.需要读取的长度 */
    private long needReadLength;
    /** 14.已经读取了的长度 */
    private long hasReadLength;
    /** 15.Etag */
    private String etag;
    /** 16.文件md5 */
    private String fileMd5;

    /** 保存文件命名方式 */
    private int saveFileMode;
    /** 重试次数 */
    private int reTryCount;
    /** 本地过期时间，毫秒 */
    private long expiredTimeMillis;
    private Context appContext;
    // 回调监听
    private DownloaderListener downloaderListener;

    public DownloaderInfo(Context appContext, String url) {
        this(appContext, url, null);
    }

    public DownloaderInfo(Context context, String url, DownloaderListener downloaderListener) {
        this.url = url;
        this.downloaderListener = downloaderListener;
        this.appContext = context.getApplicationContext();

        initDefaultValue();
    }

    private void initDefaultValue() {
        threadId = "0";
        saveFilePath = Utils.getDefaultSavePath(appContext);
        description = "";
        addTimestamp = String.valueOf(System.currentTimeMillis());
        lastModifiedTimestamp = String.valueOf(System.currentTimeMillis());
        mediaType = null;
        reason = Downloader.STATE_PENDING;
        state = Downloader.STATE_PENDING;
        contentLength = 0;
        startBytes = 0;
        needReadLength = 0;
        hasReadLength = 0;
        etag = null;

        saveFileMode = SaveFileMode.RENAME;
        reTryCount = DEFAULT_RETRY_COUNT;
        expiredTimeMillis = 0;
    }

    /** 获取线程id */
    public String getThreadId() {
        return threadId;
    }

    /** 设置线程id */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    /** 获取url */
    public String getUrl() {
        return url;
    }

    /** 设置url */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 获取文件保存路径 */
    public String getSaveFilePath() {
        return saveFilePath;
    }

    /** 设置文件保存路径 */
    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    /** 获取文件名 */
    public String getFileName() {
        return fileName;
    }

    /** 设置文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** 获取描述 */
    public String getDescription() {
        return description;
    }

    /** 设置描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 获取添加时间，毫秒 */
    public String getAddTimestamp() {
        return addTimestamp;
    }

    /** 设置添加时间，毫秒 */
    public void setAddTimestamp(String addTimestamp) {
        this.addTimestamp = addTimestamp;
    }

    /** 获取最后修改时间，毫秒 */
    public String getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    /** 设置最后修改时间，毫秒 */
    public void setLastModifiedTimestamp(String lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    /** 获取文件类型 */
    public String getMediaType() {
        return mediaType;
    }

    /** 设置文件类型 */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /** 获取原因 */
    public int getReason() {
        return reason;
    }

    /** 设置原因 */
    public void setReason(int reason) {
        this.reason = reason;
    }

    /** 获取状态 */
    public int getState() {
        return state;
    }

    /** 设置状态 */
    public void setState(int state) {
        this.state = state;
    }

    /** 获取文件长度 */
    public long getContentLength() {
        return contentLength;
    }

    /** 设置文件长度 */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /** 获取开始点 */
    public long getStartBytes() {
        return startBytes;
    }

    /** 设置下载点 */
    public void setStartBytes(long startBytes) {
        this.startBytes = startBytes;
    }

    /** 获取需要下载的长度 */
    public long getNeedReadLength() {
        return needReadLength;
    }

    /** 设置需要下载的长度 */
    public void setNeedReadLength(long needReadLength) {
        this.needReadLength = needReadLength;
    }

    /** 获取已经读取了的长度 */
    public long getHasReadLength() {
        return hasReadLength;
    }

    /** 设置已经读取了的长度 */
    public void setHasReadLength(long hasReadLength) {
        this.hasReadLength = hasReadLength;
    }

    /** 获取etag */
    public String getEtag() {
        return etag;
    }

    /** 设置etag */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /** 设置文件MD5 */
    public String getFileMd5() {
        return fileMd5;
    }

    /** 获取文件MD5 */
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    /** 获取文件保存类型 */
    public int getSaveFileMode() {
        return saveFileMode;
    }

    /** 设置文件保存类型 */
    public void setSaveFileMode(int saveFileMode) {
        this.saveFileMode = saveFileMode;
    }

    /** 获取重试次数 */
    public int getReTryCount() {
        return reTryCount;
    }

    /** 设置重试次数 */
    public void setReTryCount(int reTryCount) {
        this.reTryCount = reTryCount;
    }

    /** 获取过期时间 */
    public long getExpiredTimeMillis() {
        return expiredTimeMillis;
    }

    /** 设置过期时间 */
    public void setExpiredTimeMillis(long expiredTimeMillis) {
        this.expiredTimeMillis = expiredTimeMillis;
    }

    public Context getContext() {
        return appContext;
    }

    public void setContext(Context appContext) {
        this.appContext = appContext;
    }

    /** 获取下载监听 */
    public DownloaderListener getDownloaderListener() {
        return downloaderListener;
    }

    /** 设置下载监听 */
    public void setDownloaderListener(DownloaderListener downloaderListener) {
        this.downloaderListener = downloaderListener;
    }

    @Override
    public String toString() {
        return "DownloaderInfo{" +
                "threadId='" + threadId + '\'' +
                ", url='" + url + '\'' +
                ", saveFilePath='" + saveFilePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", description='" + description + '\'' +
                ", addTimestamp='" + addTimestamp + '\'' +
                ", lastModifiedTimestamp='" + lastModifiedTimestamp + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", reason=" + reason +
                ", state=" + state +
                ", contentLength=" + contentLength +
                ", startBytes=" + startBytes +
                ", needReadLength=" + needReadLength +
                ", hasReadLength=" + hasReadLength +
                ", etag='" + etag + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", saveFileMode=" + saveFileMode +
                ", reTryCount=" + reTryCount +
                ", expiredTimeMillis=" + expiredTimeMillis +
                ", downloaderListener=" + downloaderListener +
                '}';
    }
}
