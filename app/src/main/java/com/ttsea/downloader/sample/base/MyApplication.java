package com.ttsea.downloader.sample.base;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.ttsea.downloader.download.JDownloaderManager;
import com.ttsea.downloader.sample.Config;


/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/4/18 17:46 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 解决AsyncTask.onPostExecute不执行问题, start
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 解决AsyncTask.onPostExecute不执行问题, end

        initGlobalConfig();
    }

    /** 初始化全局变量 */
    private void initGlobalConfig() {
        if (Config.DEBUG) {
            LeakCanary.install(this);
        }
        JDownloaderManager.getInstance(this).debugMode(Config.DEBUG);
    }
}
