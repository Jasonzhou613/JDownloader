package com.ttsea.downloader.sample.download;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;


import com.ttsea.downloader.download.JDownloadLog;
import com.ttsea.downloader.download.Downloader;
import com.ttsea.downloader.download.DownloaderInfo;
import com.ttsea.downloader.download.SaveFileMode;
import com.ttsea.downloader.listener.DownloaderListener;
import com.ttsea.downloader.sample.DigitUtils;
import com.ttsea.downloader.sample.R;

/**
 * 单个下载 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/13 17:10 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/13 17:10.
 */
public class SingleDownloadActivity extends Activity implements View.OnClickListener {
    private final String TAG = "SingleDownloadActivity";

    private EditText etUrl;
    private ScrollView scInfoView;
    private ProgressBar pb;
    private TextView tvFileName;
    private TextView tvInfo;
    private Button btnDownload;
    private Button btnCancel;
    private Button btnDelete;
    private View bottomView;

    private Downloader downloader;
    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.single_download_main);

        etUrl = (EditText) findViewById(R.id.etUrl);
        scInfoView = (ScrollView) findViewById(R.id.scInfoView);
        pb = (ProgressBar) findViewById(R.id.pb);
        tvFileName = (TextView) findViewById(R.id.tvFileName);
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        bottomView = findViewById(R.id.bottomView);

        bottomView.setVisibility(View.VISIBLE);

        btnDownload.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        pb.setMax(1);
        pb.setProgress(0);

        etUrl.setText(TestDownloadUrl.TEMP_URL);

        initDownloader();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDownload:
                switchDownloaderState();
                break;

            case R.id.btnCancel:
                if (downloader != null) {
                    downloader.cancel(Downloader.ERROR_HUMAN);
                }
                break;

            case R.id.btnDelete:
                if (downloader != null) {
                    downloader.deleteFile();
                    downloader.deleteRecord();
                    stringBuilder = new StringBuilder("");
                    updateProgress(downloader.getDownloaderInfo());
                    tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                    tvInfo.setText(stringBuilder.toString());

                    scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
                }

            default:
                break;
        }
    }

    private void initDownloader() {
        String url = etUrl.getText().toString();

        final DownloaderInfo downloaderInfo = new DownloaderInfo(this.getApplicationContext(), url);
        downloaderInfo.setSaveFileMode(SaveFileMode.RENAME);

        DownloaderListener downloaderListener = new DownloaderListener() {
            boolean hasSetData = false;

            @Override
            public void onPending() {
                String msg = "等待下载..." + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onLinking() {
                String msg = "\n正在连接..." + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onStart() {
                String msg = "\n开始下载..." + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onPause(int reason) {
                String msg = "\n已暂停... reason:" + Downloader.getStateStr(reason)
                        + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onCancel(int reason) {
                String msg = "\n已取消... reason:" + Downloader.getStateStr(reason)
                        + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onComplete() {
                String msg = "\n已完成" + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onError(Throwable e) {
                String msg = "\n下载失败, e:" + e.getMessage()
                        + getSpeedHasReadProgress(downloader.getDownloaderInfo(), 0);
                stringBuilder.append(msg);
                JDownloadLog.e(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onDownloading(long hasReadLength, long needReadLength, long speedBytePerSec) {
                if (!hasSetData) {
                    hasSetData = true;
                    stringBuilder.append("\n正在下载...");
                    JDownloadLog.d(TAG, "\n正在下载...");
                }

                String msg = getSpeedHasReadProgress(downloader.getDownloaderInfo(), speedBytePerSec);
                stringBuilder.append(msg);
                JDownloadLog.d(TAG, msg);

                updateProgress(downloader.getDownloaderInfo());
                tvFileName.setText(getFileInfo(downloader.getDownloaderInfo()));
                tvInfo.setText(stringBuilder.toString());

                scInfoView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        };

        downloaderInfo.setDownloaderListener(downloaderListener);
        downloader = new Downloader(downloaderInfo);
    }

    public void switchDownloaderState() {
        if (downloader.getState() == Downloader.STATE_PENDING
                || downloader.getState() == Downloader.STATE_LINKING
                || downloader.getState() == Downloader.STATE_START
                || downloader.getState() == Downloader.STATE_DOWNLOADING) {
            downloader.pause(Downloader.PAUSED_HUMAN);
        } else {
            downloader.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloader != null) {
            if (downloader.isRunning() || downloader.getState() == Downloader.STATE_PENDING) {
                downloader.pause(Downloader.PAUSED_HUMAN);
            }
        }
    }

    private void updateProgress(DownloaderInfo info) {
        if (downloader.isRunning() || downloader.getState() == Downloader.STATE_PENDING) {
            btnDownload.setText("暂停");
        } else {
            btnDownload.setText("开始");
        }

        long needReadLength = info.getNeedReadLength();
        long hasReadLength = info.getHasReadLength();

        if (hasReadLength < 1 || needReadLength < 1) {
            pb.setMax(1);
            pb.setProgress(0);
            return;
        }
        int max, progress;
        int rate = 10000;
        if (needReadLength > Integer.MAX_VALUE - 1) {
            max = (int) (needReadLength / rate);
            progress = (int) (hasReadLength / rate);
        } else {
            max = (int) needReadLength;
            progress = (int) hasReadLength;
        }
        pb.setMax(max);
        pb.setProgress(progress);
    }

    private String getFileInfo(DownloaderInfo info) {
        String fileName = info.getFileName();
        if (Utils.isEmpty(fileName)) {
            fileName = "未知";
        }
        String contentSize = "未知";
        long contentLength = info.getContentLength();
        if (contentLength > 0) {
            contentSize = Utils.getFileSizeWithUnit(contentLength);
        }
        return "文件名:" + fileName + "\n文件大小:" + contentSize;
    }

    private String getSpeedHasReadProgress(DownloaderInfo info, long speedBytePerSec) {
        long hasReadLength = info.getHasReadLength();
        long needReadLength = info.getNeedReadLength();

        if (needReadLength == 0) {
            return "";
        }

        float percentage = DigitUtils.getFloat(((float) hasReadLength / needReadLength) * 100, 2);

        int max, progress;
        int rate = 10000;
        if (needReadLength > Integer.MAX_VALUE - 1) {
            max = (int) (needReadLength / rate);
            progress = (int) (hasReadLength / rate);
        } else {
            max = (int) needReadLength;
            progress = (int) hasReadLength;
        }

        return "\n--" + percentage + "%" + ", " + Utils.getSpeedWithUnit(speedBytePerSec)
                + "\n  hasRead:" + hasReadLength + ", needRead:" + needReadLength
                + "\n  progress:" + progress + ", max:" + max;
    }
}