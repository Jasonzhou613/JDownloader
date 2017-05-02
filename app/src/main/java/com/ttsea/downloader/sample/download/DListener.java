package com.ttsea.downloader.sample.download;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/5/2 16:52 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 */
public class DListener implements com.ttsea.downloader.listener.DownloaderListener {
    private DownloaderAdapter adapter;
    private String url;

    public DListener(DownloaderAdapter adapter, String url) {
        this.adapter = adapter;
        this.url = url;
    }

    @Override
    public void onPending() {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onLinking() {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onStart() {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onPause(int reason) {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onCancel(int reason) {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onComplete() {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onError(Throwable e) {
        adapter.notifyItemChanged(url);
    }

    @Override
    public void onDownloading(long hasReadLength, long needReadLength, long speedBytePerSec) {
        adapter.notifyItemChanged(url);
    }
}
