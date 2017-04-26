package com.ttsea.downloader.sample.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
    private ListView lvList;

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
        lvList = (ListView) findViewById(R.id.lvList);

        btnAddCustom.setOnClickListener(this);
        btnAddDefault.setOnClickListener(this);
        btnStartAll.setOnClickListener(this);
        btnPauseAll.setOnClickListener(this);
        btnCancelAll.setOnClickListener(this);
        btnDeleteAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddCustom://添加一个自定义的下载任务
                break;

            case R.id.btnAddDefault://添加一个已经定义好的下载任务，如 urls
                break;

            case R.id.btnStartAll://开始全部下载任务
                break;

            case R.id.btnPauseAll://暂停全部下载任务
                break;

            case R.id.btnCancelAll://取消全部下载任务
                break;

            case R.id.btnDeleteAll://删除所有下载任务
                break;

            default:
                break;
        }
    }
}