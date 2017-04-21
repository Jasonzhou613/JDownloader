package com.ttsea.downloader.download;


import com.ttsea.downloader.db.DownloadDBHelper;
import com.ttsea.downloader.exception.DownloadException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * 下载单元，这里负责正真的下载工作 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/20 15:08 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/20 15:08.
 */
class DownloadThread {
    private final String TAG = "DownloadThread";

    private Downloader downloader;
    private HttpOption httpOption;
    private DownloaderInfo downloaderInfo;
    private DownloadSubscriber<DownloaderInfo> subscriber;

    public DownloadThread(Downloader downloader) {
        this.downloader = downloader;
        this.httpOption = downloader.getHttpOption();
        this.downloaderInfo = downloader.getDownloaderInfo();
        this.subscriber = downloader.getSubscriber();
    }

    /** 开始真正的下载并将文件写入到指定的保存位置 */
    public void startDownload() {

        long start = downloaderInfo.getStartBytes() + downloaderInfo.getHasReadLength();
        long end = downloaderInfo.getStartBytes() + downloaderInfo.getNeedReadLength();
        String rang = "bytes=" + start + "-" + end;

        JDownloadLog.d(TAG, "start, rang:" + rang + ", downloaderInfo:" + downloaderInfo.toString());

        RetrofitClient.getRetrofit(httpOption)
                .create(HttpDownService.class)
                .download(rang, downloaderInfo.getUrl())
                .retry(downloaderInfo.getReTryCount())
                .subscribeOn(Schedulers.io())
                .map(new Function<Response<ResponseBody>, DownloaderInfo>() {
                    @Override
                    public DownloaderInfo apply(Response<ResponseBody> response) throws Exception {
                        // http code 不在[200,300)之间表示响应码有误
                        if (!response.isSuccessful()) {
                            String msg = Downloader.getStateStr(Downloader.ERROR_UNHANDLED_HTTP_CODE) + ", code:" + response.code();
                            downloader.cancel(Downloader.STATE_FAILED, Downloader.ERROR_UNHANDLED_HTTP_CODE, msg);
                            throw new DownloadException(downloader.getState(), downloader.getReason(), msg);
                        }

                        ResponseBody responseBody = response.body();

                        try {
                            if (downloader.getState() == Downloader.STATE_PAUSED || downloader.getState() == Downloader.STATE_CANCEL
                                    || downloader.getState() == Downloader.STATE_FAILED) {
                                String errorMsg = "download has " + Downloader.getStateStr(downloader.getState())
                                        + ", reason:" + Downloader.getStateStr(downloader.getReason());
                                JDownloadLog.e(TAG, errorMsg);
                                throw new DownloadException(downloader.getState(), downloader.getReason(), errorMsg);
                            }

                            JDownloadLog.d(TAG, "start download...");
                            downloaderInfo.setState(Downloader.STATE_DOWNLOADING);
                            DownloadDBHelper.insertOrUpdate(downloaderInfo.getContext(), downloaderInfo);
                            subscriber.onDownloading(downloaderInfo.getHasReadLength(), downloaderInfo.getNeedReadLength(), 0);

                            writeFile(responseBody, downloaderInfo);

                        } finally {
                            responseBody.close();
                        }
                        return downloaderInfo;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /** 开始将responseBody写入文件 */
    private void writeFile(ResponseBody responseBody, DownloaderInfo info) throws IOException, DownloadException {
        File file = new File(info.getSaveFilePath(), info.getFileName());

        FileChannel channelOut;
        RandomAccessFile randomAccessFile;
        randomAccessFile = new RandomAccessFile(file, "rwd");
        channelOut = randomAccessFile.getChannel();
        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                info.getHasReadLength(), info.getNeedReadLength() - info.getHasReadLength());

        byte[] buffer = new byte[1024 * 8];
        int len;

        try {
            while ((len = responseBody.byteStream().read(buffer)) != -1) {
                if (!file.exists()) {
                    throw new IOException("file not exit, file:" + file.getAbsolutePath());
                }

                mappedBuffer.put(buffer, 0, len);

                downloaderInfo.setHasReadLength(downloaderInfo.getHasReadLength() + len);
                subscriber.onDownloading(downloaderInfo.getHasReadLength(), downloaderInfo.getNeedReadLength(), len);

                if (downloader.getState() != Downloader.STATE_DOWNLOADING) {
                    DownloadDBHelper.insertOrUpdate(downloaderInfo.getContext(), downloaderInfo);

                    String errorMsg = Downloader.getStateStr(downloader.getReason());
                    JDownloadLog.e(TAG, "DownloadThread exception, e:" + errorMsg);
                    throw new DownloadException(downloader.getState(), downloader.getReason(), errorMsg);
                }
            }

            JDownloadLog.d(TAG, "download successful, filePath:" + file.getAbsolutePath());

        } finally {
            try {
                responseBody.byteStream().close();
                channelOut.close();
                randomAccessFile.close();

            } catch (Exception e) {
                JDownloadLog.e(TAG, "Exception e:" + e.getMessage());
            }
        }
    }
}
