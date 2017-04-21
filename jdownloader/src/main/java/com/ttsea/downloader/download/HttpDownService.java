package com.ttsea.downloader.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 16:45 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 16:45.
 */
interface HttpDownService {

    /**
     * 获取文件信息
     *
     * @param url 下载地址
     * @return Observable
     */
    @Streaming/*大文件需要加入这个判断，防止下载过程中写入到内存中*/
    @GET
    Observable<Response<ResponseBody>> getFileInfo(@Url String url);

    /**
     * 断点续传下载接口
     *
     * @param range 下载范围，如："bytes=0-1024"
     * @param url   下载地址
     * @return Observable
     */
    @Streaming/*大文件需要加入这个判断，防止下载过程中写入到内存中*/
    @GET
    Observable<Response<ResponseBody>> download(@Header("RANGE") String range, @Url String url);
}
