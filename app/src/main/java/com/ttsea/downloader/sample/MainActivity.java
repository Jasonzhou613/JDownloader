package com.ttsea.downloader.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ttsea.downloader.sample.download.MultiDownloadActivity;
import com.ttsea.downloader.sample.download.SingleDownloadActivity;


public class MainActivity extends Activity implements View.OnClickListener {
    private Button btnSingleDownload;
    private Button btnMultiDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnSingleDownload = (Button) findViewById(R.id.btnSingleDownload);
        btnMultiDownload = (Button) findViewById(R.id.btnMultiDownload);

        btnSingleDownload.setOnClickListener(this);
        btnMultiDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.btnSingleDownload:
                intent = new Intent(this, SingleDownloadActivity.class);
                startActivity(intent);
                break;

            case R.id.btnMultiDownload:
                intent = new Intent(this, MultiDownloadActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
