package com.ttsea.downloader.download;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ttsea.downloader.exception.DownloadException;
import com.ttsea.downloader.listener.DownloaderListener;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 订阅者 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 17:18 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 17:18.
 */
class DownloadSubscriber<T> implements Observer<T>, DownloaderListener {
    private final String TAG = "DownloadSubscriber";

    private DownloaderInfo downloaderInfo;
    private Disposable disposable;
    private ProgressHandler progressHandler;
    private long lastHasReadLength;
    private final NeedUpdateUI needUpdateUI = new NeedUpdateUI();

    public DownloadSubscriber(DownloaderInfo info) {
        this.downloaderInfo = info;

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        progressHandler = new ProgressHandler(DownloadSubscriber.this);
    }

    @Override
    public void onPending() {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onPending();
                }
                progressHandler.startSendMessage();
            }
        };
    }

    @Override
    public void onLinking() {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onLinking();
                }
                progressHandler.startSendMessage();
            }
        };
    }

    @Override
    public void onStart() {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onStart();
                }
                progressHandler.startSendMessage();
            }
        };
    }

    /** 开始订阅 */
    @Override
    public void onSubscribe(final Disposable d) {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    //这里不用调用onStart方法，已经在Downloader中调用了
                    //downloaderInfo.getDownloaderListener().onStart();
                }
                disposable = d;
                progressHandler.startSendMessage();
            }
        };
    }

    @Override
    public void onPause(final int reason) {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                progressHandler.stopSendMessage();

                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onPause(reason);
                }

                //暂停后，自动适配下载管理器
                JDownloaderManager.getInstance(downloaderInfo.getContext()).fit();
            }
        };
    }

    @Override
    public void onCancel(final int reason) {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                progressHandler.stopSendMessage();

                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onCancel(reason);
                }

                //取消后，自动适配下载管理器
                JDownloaderManager.getInstance(downloaderInfo.getContext()).fit();
            }
        };
    }

    @Override
    public void onDownloading(final long hasReadLength, final long needReadLength, long speedBytePerSec) {
        synchronized (needUpdateUI) {
            if (needUpdateUI.needUpdateUI) {
                new ChangeToMainThread() {
                    @Override
                    void changeComplete() {
                        if (downloaderInfo.getDownloaderListener() != null) {
                            downloaderInfo.getDownloaderListener().onDownloading(hasReadLength, needReadLength, hasReadLength - lastHasReadLength);
                            lastHasReadLength = hasReadLength;
                        }
                    }
                };

                needUpdateUI.needUpdateUI = false;
            }
        }
    }

    @Override
    public void onNext(T value) {
        JDownloadLog.d(TAG, "onNext, nothing to do, threadName:" + Thread.currentThread().getName());
    }

    @Override
    public void onError(final Throwable e) {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    if (e instanceof DownloadException) {
                        DownloadException de = (DownloadException) e;
                        if (de.getState() == Downloader.STATE_PAUSED) {
                            downloaderInfo.getDownloaderListener().onPause(de.getReason());

                        } else if (de.getState() == Downloader.STATE_CANCEL) {
                            downloaderInfo.getDownloaderListener().onCancel(de.getReason());

                        } else {
                            downloaderInfo.getDownloaderListener().onError(de);

                        }
                    } else {
                        downloaderInfo.getDownloaderListener().onError(e);
                    }
                }
                //下载失败后，自动适配下载管理器
                JDownloaderManager.getInstance(downloaderInfo.getContext()).fit();
                progressHandler.stopSendMessage();
            }
        };
    }

    @Override
    public void onComplete() {
        new ChangeToMainThread() {
            @Override
            void changeComplete() {
                if (downloaderInfo.getDownloaderListener() != null) {
                    downloaderInfo.getDownloaderListener().onComplete();
                }
                progressHandler.stopSendMessage();
                //下载完后要自动适配下载管理器
                JDownloaderManager.getInstance(downloaderInfo.getContext()).fit();
            }
        };
    }

    public Disposable getDisposable() {
        return disposable;
    }

    /** 取消订阅关系 */
    private void disposable() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    /** 是否取消了订阅关系 */
    public boolean isDisposable() {
        if (disposable == null) {
            return true;
        }

        return disposable.isDisposed();
    }

    abstract class ChangeToMainThread {

        ChangeToMainThread() {
            init();
        }

        void init() {
            Observable.just("")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            //nothing to do
                        }

                        @Override
                        public void onNext(String value) {
                            //nothing to do
                        }

                        @Override
                        public void onError(Throwable e) {
                            //nothing to do
                        }

                        @Override
                        public void onComplete() {
                            changeComplete();
                        }
                    });
        }

        abstract void changeComplete();
    }

    class NeedUpdateUI {
        boolean needUpdateUI = true;
    }

    private static class ProgressHandler extends Handler {
        private final int MESSAGE_WHAT = 0x11;
        private final int PERIOD_MILLIS = 1000;
        private WeakReference<DownloadSubscriber> subscriberWf;
        private boolean isStop = true;

        ProgressHandler(DownloadSubscriber subscriber) {
            subscriberWf = new WeakReference<DownloadSubscriber>(subscriber);
        }

        void startSendMessage() {
            if (isStop) {
                isStop = false;
                sendEmptyMessage(MESSAGE_WHAT);
            }
        }

        void stopSendMessage() {
            if (!isStop) {
                isStop = true;
            }
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadSubscriber subscriber = subscriberWf.get();
            if (subscriber == null || msg.what != MESSAGE_WHAT) {
                return;
            }

            synchronized (subscriber.needUpdateUI) {
                subscriber.needUpdateUI.needUpdateUI = true;
            }

            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isStop) {
                        sendEmptyMessage(MESSAGE_WHAT);
                    }
                }
            }, PERIOD_MILLIS);
        }
    }
}
