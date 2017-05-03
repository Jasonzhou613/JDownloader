package com.ttsea.downloader.download;


import com.ttsea.downloader.db.DownloadDBHelper;
import com.ttsea.downloader.exception.DownloadException;
import com.ttsea.downloader.http.Http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;


/**
 * 文件下载器 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 16:55 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 16:55.
 */
public class Downloader implements TaskHandler {
    private final String TAG = "Downloader";

    // Downloader status，数据库字段COLUMN_STATE的值,其值范围：0x010-0x029
    /** 等待下载，默认状态 */
    public final static int STATE_PENDING = 0x010;//等待下载--16
    /** 开始连接，该状态中会获取下载信息，如文件名、文件长度等 */
    public final static int STATE_LINKING = 0x011;//开始链接--17
    /** 开始下载 */
    public final static int STATE_START = 0x012;//开始下载--18
    /** 正在下载 */
    public final static int STATE_DOWNLOADING = 0x013;//正在下载--19
    /** 暂停 */
    public final static int STATE_PAUSED = 0x014;//暂停--20
    /** 取消 */
    public final static int STATE_CANCEL = 0x015;//取消--21
    /** 下载成功 */
    public final static int STATE_SUCCESSFUL = 0x016;//下载成功--22
    /** 下载失败 */
    public final static int STATE_FAILED = 0x017;//下载失败--23

    //Downloader错误缘由，数据库字段COLUMN_REASON的值,其值范围：0x030-0x049
    /** 未知的错误导致下载失败，默认状态 */
    public final static int ERROR_UNKNOWN = 0x030;//未知--48
    /** 响应code有误导致下载失败，如 416等 */
    public final static int ERROR_UNHANDLED_HTTP_CODE = 0x031;//中断--49
    /** http数据有误导致下载失败 */
    public final static int ERROR_HTTP_DATA_ERROR = 0x032;//中断--50
    /** 没有SD卡导致下载失败 */
    public final static int ERROR_DEVICE_NOT_FOUND = 0x033;//中断--51
    /** 空间不足导致下载取消 */
    public final static int ERROR_INSUFFICIENT_SPACE = 0x034;//空间不足--52
    /** 下载文件已经存在，导致下载失败 */
    public final static int ERROR_FILE_ALREADY_EXISTS = 0x035;//文件已存在--53
    /** 用户主动取消下载 */
    public final static int ERROR_HUMAN = 0x036;//中断--54

    //Downloader暂停理由，数据库字段COLUMN_REASON的值,其值范围：0x050-0x069
    /** 网络不可用导致下载暂停 */
    public final static int PAUSED_WAITING_FOR_NETWORK = 0x050;//暂停等待网络--80
    /** wifi不可用导致下载暂停 */
    public final static int PAUSED_QUEUED_FOR_WIFI = 0x051;//暂停等待wifi--81
    /** 用户主动下载暂停 */
    public final static int PAUSED_HUMAN = 0x052;//被用户手动暂停--82
    /** 未知原因导致下载暂停 */
    public final static int PAUSED_UNKNOWN = 0x053;//未知原因导致暂停--83

    private DownloaderInfo downloaderInfo;
    private HttpOption httpOption;
    private List<Disposable> disposableList;
    private DownloadSubscriber<DownloaderInfo> subscriber;

    /**
     * 实例化
     *
     * @param info 下载信息
     */
    protected Downloader(DownloaderInfo info) {
        this(info, null);
    }

    /**
     * 实例化
     *
     * @param info       下载信息
     * @param httpOption http选项
     */
    protected Downloader(DownloaderInfo info, HttpOption httpOption) {
        if (info == null) {
            throw new NullPointerException("DownloaderInfo could not be null");
        }
        if (Utils.isEmpty(info.getUrl())) {
            throw new NullPointerException("downloaderInfo.url could not be null");
        }
        if (httpOption == null) {
            httpOption = new HttpOption.Builder().build();
        }

        this.downloaderInfo = info;
        this.httpOption = httpOption;
        this.disposableList = new ArrayList<>();

        subscriber = new DownloadSubscriber<DownloaderInfo>(this.downloaderInfo) {
            @Override
            public void onError(Throwable e) {

                //如果不是自定义的异常，我们需要主动取消下载
                if (e == null || !(e instanceof DownloadException)) {
                    String errorMsg = getStateStr(Downloader.ERROR_UNKNOWN) + ", e:" + (e == null ? "" : e.getMessage());
                    cancel(Downloader.STATE_FAILED, Downloader.ERROR_UNKNOWN, errorMsg);
                }

                super.onError(e);
            }

            @Override
            public void onComplete() {
                //下载完成设置状态
                setState(Downloader.STATE_SUCCESSFUL);
                setReason(Downloader.STATE_SUCCESSFUL);

                //保存下载信息
                saveDownloadInfo();
                //更新文件md5
                updateFileMD5();
                super.onComplete();
            }
        };

        initState();
    }

    /** 初始化状态 */
    private void initState() {

        //首先从数据库里查询出来是否有下载记录
        //1.如果已有下载记录，则将读取出来的相关信息赋值给downloaderInfo，并调用对应的监听方法
        //2.如果没有下载记录，则直接保存downloaderInfo，并调用对应的监听方法

        DownloaderInfo info = DownloadDBHelper.getDownloaderInfo(downloaderInfo.getContext(),
                downloaderInfo.getUrl(), downloaderInfo.getThreadId());

        //如果info不为空，则将info里全部的数据库字段数据赋值给downloaderInfo
        if (info != null) {
            JDownloadLog.d(TAG, "get downloader info from record, info:" + info.toString());

            downloaderInfo.setThreadId(info.getThreadId());
            downloaderInfo.setUrl(info.getUrl());
            downloaderInfo.setSaveFilePath(info.getSaveFilePath());
            downloaderInfo.setFileName(info.getFileName());
            downloaderInfo.setDescription(info.getDescription());
            downloaderInfo.setAddTimestamp(info.getAddTimestamp());
            downloaderInfo.setLastModifiedTimestamp(info.getLastModifiedTimestamp());
            downloaderInfo.setMediaType(info.getMediaType());
            downloaderInfo.setReason(info.getReason());
            downloaderInfo.setState(info.getState());
            downloaderInfo.setContentLength(info.getContentLength());
            downloaderInfo.setStartBytes(info.getStartBytes());
            downloaderInfo.setNeedReadLength(info.getNeedReadLength());
            downloaderInfo.setHasReadLength(info.getHasReadLength());
            downloaderInfo.setEtag(info.getEtag());

            //如果文件不存在了，则需要重新下载
            if (!isFileExist()) {
                JDownloadLog.d(TAG, "need reset hasReadLength as 0.");
                downloaderInfo.setHasReadLength(0);
                setState(STATE_PENDING);
                setReason(STATE_PENDING);
            }

        } else {
            JDownloadLog.d(TAG, "add a new downloader.");
        }

        saveDownloadInfo();

        switch (getState()) {
            case Downloader.STATE_PAUSED:
                subscriber.onPause(getReason());
                break;

            case Downloader.STATE_CANCEL:
                subscriber.onCancel(getReason());
                break;

            case Downloader.STATE_SUCCESSFUL:
                subscriber.onComplete();
                break;

            case Downloader.STATE_FAILED:
                subscriber.onError(new DownloadException(downloaderInfo.getState(),
                        downloaderInfo.getReason(), getStateStr(downloaderInfo.getReason())));
                break;

            default:
                setState(Downloader.STATE_PENDING);
                setReason(Downloader.STATE_PENDING);
                subscriber.onPending();
                break;
        }
    }

    /** 开始下载 */
    @Override
    public void start() {
        if (isRunning()) {
            JDownloadLog.d(TAG, "downloader already start, state:" + getStateStr(getState()));
            return;
        }

        if (getState() == Downloader.STATE_FAILED) {
            if (getReason() == Downloader.ERROR_UNHANDLED_HTTP_CODE) {
                resetStatus();
            }
        }

        //文件是否已经下载完成
        if (isFileExist()
                && downloaderInfo.getContentLength() > 0
                && downloaderInfo.getNeedReadLength() > 0
                && downloaderInfo.getNeedReadLength() == downloaderInfo.getHasReadLength()
            //&& checkMD5()//大文件计算md5太费时，这里先忽略md5校验
                ) {

            JDownloadLog.d(TAG, "downloader already completed.");
            setState(Downloader.STATE_SUCCESSFUL);
            setReason(Downloader.STATE_SUCCESSFUL);
            saveDownloadInfo();
            subscriber.onStart();
            subscriber.onDownloading(downloaderInfo.getHasReadLength(),
                    downloaderInfo.getNeedReadLength(), downloaderInfo.getNeedReadLength());
            subscriber.onComplete();
            //表示已经下载完成，直接返回
            return;
        }

        //是否需要重新获取文件信息
        boolean needGetFileInfo = true;

        if (!Utils.isEmpty(downloaderInfo.getFileName())
                && downloaderInfo.getContentLength() > 0
                && downloaderInfo.getNeedReadLength() > 0) {
            needGetFileInfo = false;
        }

        setState(Downloader.STATE_LINKING);
        setReason(Downloader.STATE_LINKING);
        subscriber.onLinking();

        if (needGetFileInfo) {
            JDownloadLog.d(TAG, "need get file info by url.");
            getFileInfo();

        } else {
            //是否需要重新下载
            if (!isFileExist()) {
                JDownloadLog.d(TAG, "file not exist, need reDownload....");
                downloaderInfo.setHasReadLength(0);
            } else {
                JDownloadLog.d(TAG, "file already exist, continue to download....");
            }
            startDownloadThread();
        }
    }

    @Override
    public void pause(int reason) {
        if (isPaused()) {
            JDownloadLog.d(TAG, "downloader already paused, state:" + getStateStr(getState()));
            return;
        }

        dispose();
        setState(Downloader.STATE_PAUSED);
        setReason(reason);
        saveDownloadInfo();
        subscriber.onPause(reason);

        JDownloadLog.d(TAG, "downloader paused, downloaderInfo:" + downloaderInfo.toString());
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public void cancel(int reason) {
        cancel(Downloader.STATE_CANCEL, reason, getStateStr(reason));
    }

    public void cancel(int state, int reason, String reasonStr) {
        if (isCancelled()) {
            JDownloadLog.d(TAG, "downloader already canceled, state:" + getStateStr(getState()));
            return;
        }

        dispose();
        setState(state);
        setReason(reason);
        saveDownloadInfo();
        subscriber.onCancel(reason);

        JDownloadLog.d(TAG, "downloader canceled, reason:" + reasonStr
                + ", downloaderInfo:" + downloaderInfo.toString());
    }

    @Override
    public void reDownload() {
        cancel(Downloader.STATE_PAUSED, Downloader.ERROR_HTTP_DATA_ERROR, getStateStr(Downloader.ERROR_HTTP_DATA_ERROR));
        resetStatus();
        start();
    }

    /** 重设Downloader的状态 */
    private void resetStatus() {
        JDownloadLog.d(TAG, "reset downloader status.");

        downloaderInfo.setFileName(null);
        downloaderInfo.setDescription(null);
        downloaderInfo.setMediaType(null);

        downloaderInfo.setContentLength(0);
        downloaderInfo.setStartBytes(0);
        downloaderInfo.setNeedReadLength(0);
        downloaderInfo.setHasReadLength(0);
        downloaderInfo.setEtag(null);
        downloaderInfo.setFileMd5(null);

        setState(Downloader.STATE_PENDING);
        setReason(Downloader.STATE_PENDING);

        saveDownloadInfo();
    }

    @Override
    public boolean isPaused() {
        return downloaderInfo.getState() == STATE_PAUSED;
    }

    @Override
    public boolean isCancelled() {
        return downloaderInfo.getState() == STATE_CANCEL;
    }

    /** 取消订阅 */
    private void dispose() {
        while (!disposableList.isEmpty()) {
            Disposable disposable = disposableList.remove(0);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    /**
     * 通过url从服务器中拿到文件相关信息<br>
     * 如：长度、文件名、MediaType和Etag等信息<br>
     * 拿到文件信息后，保存到数据库中，开始正真的下载工作<br>
     */
    private void getFileInfo() {
        JDownloadLog.d(TAG, "getFileInfo, url:" + downloaderInfo.getUrl());
        RetrofitClient.getRetrofit(httpOption)
                .create(HttpDownService.class)
                .getFileInfo(downloaderInfo.getUrl())
                .retry(downloaderInfo.getReTryCount())
                .subscribeOn(Schedulers.io())
                .map(new Function<retrofit2.Response<ResponseBody>, DownloaderInfo>() {
                    @Override
                    public DownloaderInfo apply(retrofit2.Response<ResponseBody> response) throws Exception {
                        // http code 不在[200,300)之间表示响应码有误
                        if (!response.isSuccessful()) {
                            String msg = getStateStr(Downloader.ERROR_UNHANDLED_HTTP_CODE) + ", code:" + response.code();
                            cancel(Downloader.STATE_FAILED, Downloader.ERROR_UNHANDLED_HTTP_CODE, msg);
                            throw new DownloadException(getState(), getReason(), msg);
                        }

                        ResponseBody responseBody = response.body();

                        try {
                            MediaType mediaType = responseBody.contentType();
                            if (mediaType != null) {
                                downloaderInfo.setMediaType(mediaType.toString());
                            }
                            downloaderInfo.setContentLength(responseBody.contentLength());
                            downloaderInfo.setNeedReadLength(responseBody.contentLength());
                            downloaderInfo.setStartBytes(0);
                            downloaderInfo.setHasReadLength(0);
                            if (response.headers() != null) {
                                downloaderInfo.setEtag(response.headers().get(Http.ResponseHeadField.ETag));
                            }

                            //通过url拿到文件名
                            Request request = response.raw().request();
                            JDownloadLog.d(TAG, "request:" + request.toString() + "\n pathSegments:" + request.url().pathSegments());

                            String filename = request.url().pathSegments().get(request.url().pathSegments().size() - 1);
                            if (!Utils.isEmpty(filename)) {
                                JDownloadLog.d(TAG, "Get fileName from request, fileName:" + filename);
                                downloaderInfo.setFileName(filename);
                            } else {
                                JDownloadLog.d(TAG, "Get fileName from the url immediately.");
                                downloaderInfo.setFileName(Utils.getFileName(downloaderInfo.getUrl()));
                            }

                            renameIfNeed(downloaderInfo);
                            downloaderInfo.setDescription(downloaderInfo.getFileName());
                            //将获取到的文件信息保存到数据库中
                            saveDownloadInfo();

                            JDownloadLog.d(TAG, "getFileInfo, downloaderInfo:" + downloaderInfo.toString());

                            if (!SdStatusUtils.isSDAvailable()) {
                                resetStatus();
                                String errorMsg = getStateStr(Downloader.ERROR_DEVICE_NOT_FOUND) + ", SD card is not available";
                                cancel(Downloader.STATE_FAILED, Downloader.ERROR_DEVICE_NOT_FOUND, errorMsg);
                                JDownloadLog.d(TAG, errorMsg);
                                throw new DownloadException(getState(), getReason(), errorMsg);
                            }

                            //判读SD卡空间是否足够
                            long needSpaceMB = downloaderInfo.getContentLength() / 1024 / 1024 + 10;
                            if (!SdStatusUtils.isABlockEnough(needSpaceMB)) {
                                resetStatus();
                                String errorMsg = getStateStr(Downloader.ERROR_INSUFFICIENT_SPACE)
                                        + ", SD card space is no enough, need " + needSpaceMB + "MB";
                                cancel(Downloader.STATE_FAILED, Downloader.ERROR_INSUFFICIENT_SPACE, errorMsg);
                                JDownloadLog.e(TAG, errorMsg);
                                throw new DownloadException(getState(), getReason(), errorMsg);
                            }

                        } finally {
                            responseBody.close();
                        }

                        return downloaderInfo;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DownloadSubscriber<DownloaderInfo>(downloaderInfo) {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableList.add(d);
                    }

                    @Override
                    public void onNext(DownloaderInfo value) {
                        super.onNext(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        startDownloadThread();
                    }
                });
    }

    /** 启动DownloadThread，开始正真的下载工作 */
    private void startDownloadThread() {
        if (isCancelled() || isPaused()) {
            JDownloadLog.d(TAG, "Downloader has paused or cancelled, state:" + getStateStr(getState()));
            return;
        }
        setState(Downloader.STATE_START);
        setReason(Downloader.STATE_START);
        saveDownloadInfo();
        subscriber.onStart();
        JDownloadLog.d(TAG, "startDownloadThread... state:" + getStateStr(getState()));

        DownloadThread thread = new DownloadThread(this);
        thread.startDownload();
    }

    /** 如果有要，则重命名文件 */
    private void renameIfNeed(DownloaderInfo info) throws DownloadException {
        File file = new File(info.getSaveFilePath(), info.getFileName());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        boolean isFileExists = file.exists();
        String fileName = info.getFileName();

        //文件已经存在，并且保存方式为 NONACTION，则取消该下载
        if (isFileExists && info.getSaveFileMode() == SaveFileMode.NONACTION) {
            resetStatus();
            String errorMsg = "file already exists, downloader will cancel, file:" + file.getAbsolutePath();
            cancel(Downloader.STATE_FAILED, Downloader.ERROR_FILE_ALREADY_EXISTS, errorMsg);
            JDownloadLog.e(TAG, "Downloader exception, e:" + errorMsg);
            throw new DownloadException(getState(), getReason(), errorMsg);

            //文件已经存在，并且保存方式为 OVERRIDE，则覆盖该文件
        } else if (isFileExists && info.getSaveFileMode() == SaveFileMode.OVERRIDE) {
            JDownloadLog.d(TAG, "file already exists and it will be override");
            file.delete();

            //文件已经存在，并且保存方式为 RENAME，则重名该文件
        } else if (isFileExists && info.getSaveFileMode() == SaveFileMode.RENAME) {
            JDownloadLog.d(TAG, "file already exists and it will be renamed");
            int fileTag = 1;
            //重命名文件，直到该文件不存在
            while (file.exists() && fileTag < Integer.MAX_VALUE) {
                String suffix = "";
                String tempName = "";
                int index = fileName.lastIndexOf(".");

                if (index > -1) {
                    suffix = fileName.substring(index);
                    tempName = fileName.substring(0, index);
                }
                tempName = tempName + "（" + String.valueOf(fileTag) + "）" + suffix;
                file = new File(info.getSaveFilePath(), tempName);
                fileTag++;
                if (!file.exists()) {
                    fileName = tempName;
                }
            }
            //设置文件的新名字
            info.setFileName(fileName);
            JDownloadLog.d(TAG, "renamed file name:" + info.getFileName());

            //文件已经存在，默认处理方式:覆盖
        } else if (isFileExists) {
            file.delete();
        }
    }

    /** 更新文件的md5 */
    private void updateFileMD5() {
        File file = new File(downloaderInfo.getSaveFilePath(), downloaderInfo.getFileName());

        //大文件计算md5是比较耗时的，所以这里使用RxJava框架来处理
        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .map(new Function<File, String>() {
                    @Override
                    public String apply(File f) throws Exception {
                        return MD5Utils.getFileMD5(f);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                JDownloadLog.d(TAG, "file md5: " + s);
                                if (downloaderInfo.getState() == Downloader.STATE_SUCCESSFUL
                                        && !Utils.isEmpty(s)) {
                                    downloaderInfo.setFileMd5(s);
                                    saveDownloadInfo();
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable e) throws Exception {
                                JDownloadLog.e(TAG, "Throwable e:" + e.getMessage());
                            }
                        });
    }

    /** 保存下载信息 */
    public long saveDownloadInfo() {
        return DownloadDBHelper.insertOrUpdate(downloaderInfo.getContext(), downloaderInfo);
    }

    /**
     * 删除下载来的文件
     *
     * @return 删除成功返回true，否则返回false
     */
    public boolean deleteFile() {
        if (!Utils.isEmpty(downloaderInfo.getSaveFilePath()) && !Utils.isEmpty(downloaderInfo.getFileName())) {
            File file = new File(downloaderInfo.getSaveFilePath(), downloaderInfo.getFileName());
            if (file.exists()) {
                JDownloadLog.d(TAG, "delete file, file:" + file.getAbsolutePath());
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 删除下载记录
     *
     * @return 删除的记录条数
     */
    public long deleteRecord() {
        downloaderInfo.setHasReadLength(0);
        return DownloadDBHelper.deleteRecord(downloaderInfo.getContext(), downloaderInfo.getUrl());
    }

    /** 下载文件是否已经存在 */
    private boolean isFileExist() {
        if (!Utils.isEmpty(downloaderInfo.getSaveFilePath())) {
            File parentFile = new File(downloaderInfo.getSaveFilePath());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
        }

        if (!Utils.isEmpty(downloaderInfo.getFileName())) {
            File file = new File(downloaderInfo.getSaveFilePath(), downloaderInfo.getFileName());
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMD5() {
        String md5 = MD5Utils.getFileMD5(new File(downloaderInfo.getSaveFilePath(), downloaderInfo.getFileName()));
        if ((!Utils.isEmpty(md5)) && md5.equals(downloaderInfo.getFileMd5())) {
            return true;
        }
        return false;
    }

    /** 获取下载地址 */
    public String getUrl() {
        return downloaderInfo.getUrl();
    }

    /** 获取Downloader状态 */
    public int getState() {
        return downloaderInfo.getState();
    }

    /** 设置Downloader状态 */
    public void setState(int state) {
        downloaderInfo.setState(state);
    }

    /** 获取原因，如暂停原因、失败原因等 */
    public int getReason() {
        return downloaderInfo.getReason();
    }

    /** 设置原因 */
    public void setReason(int reason) {
        downloaderInfo.setReason(reason);
    }

    /** 获取Downloader Http选项 */
    public HttpOption getHttpOption() {
        return httpOption;
    }

    /** 获取Downloader 下载信息 */
    public DownloaderInfo getDownloaderInfo() {
        return downloaderInfo;
    }

    /** 获取下载器的订阅者 */
    public DownloadSubscriber<DownloaderInfo> getSubscriber() {
        return subscriber;
    }

    public boolean isRunning() {
        return getReason() == Downloader.STATE_LINKING || getState() == Downloader.STATE_START
                || getState() == Downloader.STATE_DOWNLOADING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Downloader that = (Downloader) o;

        return that.getDownloaderInfo().getUrl().equals(getDownloaderInfo().getUrl());
    }

    public static String getStateStr(int reason) {
        String reasonStr;
        switch (reason) {
            case Downloader.STATE_PENDING:
                reasonStr = "(STATE_PENDING:" + STATE_PENDING + ")";
                break;
            case Downloader.STATE_LINKING:
                reasonStr = "(STATE_LINKING:" + STATE_LINKING + ")";
                break;
            case Downloader.STATE_START:
                reasonStr = "(STATE_START:" + STATE_START + ")";
                break;
            case Downloader.STATE_DOWNLOADING:
                reasonStr = "(STATE_DOWNLOADING:" + STATE_DOWNLOADING + ")";
                break;
            case Downloader.STATE_PAUSED:
                reasonStr = "(STATE_PAUSED:" + STATE_PAUSED + ")";
                break;
            case Downloader.STATE_CANCEL:
                reasonStr = "(STATE_CANCEL:" + STATE_CANCEL + ")";
                break;
            case Downloader.STATE_SUCCESSFUL:
                reasonStr = "(STATE_SUCCESSFUL:" + STATE_SUCCESSFUL + ")";
                break;
            case Downloader.STATE_FAILED:
                reasonStr = "(STATE_FAILED:" + STATE_FAILED + ")";
                break;

            case Downloader.ERROR_UNKNOWN:
                reasonStr = "(ERROR_UNKNOWN:" + ERROR_UNKNOWN + ")";
                break;
            case Downloader.ERROR_UNHANDLED_HTTP_CODE:
                reasonStr = "(ERROR_UNHANDLED_HTTP_CODE:" + ERROR_UNHANDLED_HTTP_CODE + ")";
                break;
            case Downloader.ERROR_HTTP_DATA_ERROR:
                reasonStr = "(ERROR_HTTP_DATA_ERROR:" + ERROR_HTTP_DATA_ERROR + ")";
                break;
            case Downloader.ERROR_DEVICE_NOT_FOUND:
                reasonStr = "(ERROR_DEVICE_NOT_FOUND:" + ERROR_DEVICE_NOT_FOUND + ")";
                break;
            case Downloader.ERROR_INSUFFICIENT_SPACE:
                reasonStr = "(ERROR_INSUFFICIENT_SPACE:" + ERROR_INSUFFICIENT_SPACE + ")";
                break;
            case Downloader.ERROR_FILE_ALREADY_EXISTS:
                reasonStr = "(ERROR_FILE_ALREADY_EXISTS:" + ERROR_FILE_ALREADY_EXISTS + ")";
                break;
            case Downloader.ERROR_HUMAN:
                reasonStr = "(ERROR_HUMAN:" + ERROR_HUMAN + ")";
                break;

            case Downloader.PAUSED_WAITING_FOR_NETWORK:
                reasonStr = "(PAUSED_WAITING_FOR_NETWORK:" + PAUSED_WAITING_FOR_NETWORK + ")";
                break;
            case Downloader.PAUSED_QUEUED_FOR_WIFI:
                reasonStr = "(PAUSED_QUEUED_FOR_WIFI:" + PAUSED_QUEUED_FOR_WIFI + ")";
                break;
            case Downloader.PAUSED_HUMAN:
                reasonStr = "(PAUSED_HUMAN:" + PAUSED_HUMAN + ")";
                break;
            case Downloader.PAUSED_UNKNOWN:
                reasonStr = "(PAUSED_UNKNOWN:" + PAUSED_UNKNOWN + ")";
                break;

            default:
                reasonStr = "unknown";
                break;
        }

        return reasonStr;
    }
}