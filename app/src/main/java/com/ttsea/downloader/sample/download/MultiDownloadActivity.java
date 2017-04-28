package com.ttsea.downloader.sample.download;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ttsea.downloader.download.Downloader;
import com.ttsea.downloader.download.JDownloaderManager;
import com.ttsea.downloader.sample.R;


/**
 * 多个下载 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/21 21:10 <br>
 * <b>author:</b> Administrator <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/21 21:10.
 */
public class MultiDownloadActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SingleDownloadActivity";

    private EditText etUrl;
    private Button btnAddCustom;
    private Button btnAddDefault;
    private Button btnStartAll;
    private Button btnPauseAll;
    private Button btnCancelAll;
    private Button btnDeleteAll;
    private RecyclerView rcList;

    private DownloaderAdapter mAdapter;

    private int position = 0;
    private String[] urls = new String[]{
            TestDownloadUrl.TEMP_URL,
            TestDownloadUrl.DOWNLOAD_URL_20_3,
            TestDownloadUrl.DOWNLOAD_URL_4_85,
            TestDownloadUrl.DOWNLOAD_URL_3_77,
            TestDownloadUrl.DOWNLOAD_URL_0_0_824,
            TestDownloadUrl.DOWNLOAD_URL_127_17,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_download_main);

        etUrl = (EditText) findViewById(R.id.etUrl);
        btnAddCustom = (Button) findViewById(R.id.btnAddCustom);
        btnAddDefault = (Button) findViewById(R.id.btnAddDefault);
        btnStartAll = (Button) findViewById(R.id.btnStartAll);
        btnPauseAll = (Button) findViewById(R.id.btnPauseAll);
        btnCancelAll = (Button) findViewById(R.id.btnCancelAll);
        btnDeleteAll = (Button) findViewById(R.id.btnDeleteAll);
        rcList = (RecyclerView) findViewById(R.id.rcList);

        btnAddCustom.setOnClickListener(this);
        btnAddDefault.setOnClickListener(this);
        btnStartAll.setOnClickListener(this);
        btnPauseAll.setOnClickListener(this);
        btnCancelAll.setOnClickListener(this);
        btnDeleteAll.setOnClickListener(this);

        mAdapter = new DownloaderAdapter(this, JDownloaderManager.getInstance(this).getDownloaderMap());
        rcList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddCustom://添加一个自定义的下载任务
                addCustomDownloader(etUrl.getText().toString().trim());
                break;

            case R.id.btnAddDefault://添加一个已经定义好的下载任务，如 urls
                addDefaultDownloader();
                break;

            case R.id.btnStartAll://开始全部下载任务
                JDownloaderManager.getInstance(this).startAll();
                break;

            case R.id.btnPauseAll://暂停全部下载任务
                JDownloaderManager.getInstance(this).pauseAll(Downloader.PAUSED_HUMAN);
                break;

            case R.id.btnCancelAll://取消全部下载任务
                JDownloaderManager.getInstance(this).cancelAll(Downloader.ERROR_HUMAN);
                break;

            case R.id.btnDeleteAll://删除所有下载任务
                deleteAll();
                break;

            default:
                break;
        }
    }

    private void addDefaultDownloader() {
        if (position >= urls.length) {
            position = 0;
        }

        String url = urls[position];
        addCustomDownloader(url);
        position++;
    }

    private void addCustomDownloader(String url) {
        if (url == null || url.equals("")) {
            Toast.makeText(this, "下载地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (JDownloaderManager.getInstance(this).getDownloaderMap().containsKey(url)) {
            Toast.makeText(this, "任务已存在，url:" + url, Toast.LENGTH_SHORT).show();
            return;
        }

        JDownloaderManager.getInstance(this).addNewDownloader(this, url, null);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteAll() {
        View view = LayoutInflater.from(this).inflate(R.layout.multi_download_dialog, null);
        final CheckBox cbDeleteFile = (CheckBox) view.findViewById(R.id.cbDeleteFile);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(null)
                .setView(view)
                .setMessage("是否删除所有下载任务")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleteFile = cbDeleteFile.isChecked();
                        JDownloaderManager.getInstance(MultiDownloadActivity.this).removeAll(deleteFile);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        alert.create();
        alert.show();
    }
}