package com.ttsea.downloader.sample.download;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ttsea.downloader.download.Downloader;
import com.ttsea.downloader.download.DownloaderInfo;
import com.ttsea.downloader.download.JDownloaderManager;
import com.ttsea.downloader.sample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * // to do <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/4/27 16:05 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 */
class DownloaderAdapter extends RecyclerView.Adapter<DownloaderAdapter.ViewHolder> {
    private final String TAG = "DownloaderAdapter";

    private Context mContext;
    private List<Downloader> mList;
    private LayoutInflater mInflater;

    public DownloaderAdapter(Context context, Map<String, Downloader> downloaderMap) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);

        map2List(downloaderMap);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.multi_download_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int p) {
        final int position = holder.getAdapterPosition();
        final Downloader downloader = mList.get(position);
        final DownloaderInfo info = downloader.getDownloaderInfo();

        switch (downloader.getState()) {

            case Downloader.STATE_PENDING:
                holder.tvStatus.setText("等待下载...");
                holder.btnDownload.setText("暂停");
                break;
            case Downloader.STATE_LINKING:
                holder.tvStatus.setText("正在连接...");
                holder.btnDownload.setText("暂停");
                break;

            case Downloader.STATE_START:
                holder.tvStatus.setText("开始下载...");
                holder.btnDownload.setText("暂停");
                break;

            case Downloader.STATE_DOWNLOADING:
                holder.tvStatus.setText("正在下载...");
                holder.btnDownload.setText("暂停");
                break;

            case Downloader.STATE_PAUSED:
                holder.tvStatus.setText("已暂停");
                holder.btnDownload.setText("开始");
                break;

            case Downloader.STATE_CANCEL:
                holder.tvStatus.setText("已取消");
                holder.btnDownload.setText("开始");
                break;

            case Downloader.STATE_SUCCESSFUL:
                holder.tvStatus.setText("已完成");
                holder.btnDownload.setText("已完成");
                break;

            case Downloader.STATE_FAILED:
                holder.tvStatus.setText("下载失败");
                holder.btnDownload.setText("重新开始");
                break;

            default:
                break;
        }

        holder.tvInfo.setText(getFileInfo(info));

        long needReadLength = info.getNeedReadLength();
        long hasReadLength = info.getHasReadLength();

        if (hasReadLength < 1 || needReadLength < 1) {
            holder.pb.setMax(1);
            holder.pb.setProgress(0);
        } else {
            final int max, progress;
            int rate = 10000;
            if (needReadLength > Integer.MAX_VALUE - 1) {
                max = (int) (needReadLength / rate);
                progress = (int) (hasReadLength / rate);
            } else {
                max = (int) needReadLength;
                progress = (int) hasReadLength;
            }
            holder.pb.setMax(max);
            holder.pb.setProgress(progress);
        }

        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloader.getState() == Downloader.STATE_PENDING
                        || downloader.isRunning()) {
                    JDownloaderManager.getInstance(mContext).pause(info.getUrl(), Downloader.PAUSED_HUMAN);
                } else {
                    JDownloaderManager.getInstance(mContext).start(info.getUrl());
                }
                notifyItemChanged(position);
            }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JDownloaderManager.getInstance(mContext).cancel(downloader.getUrl(), Downloader.ERROR_HUMAN);
                notifyItemChanged(position);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(downloader.getUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private String getFileInfo(DownloaderInfo info) {
        String fileName = info.getFileName();
        if (Utils.isEmpty(fileName)) {
            fileName = "未知";
        }

        String hasRead = Utils.getFileSizeWithUnit(info.getHasReadLength());

        long contentLength = info.getContentLength();
        if (contentLength > 0) {
            return "文件名:" + fileName + ", " +
                    hasRead + "/" + Utils.getFileSizeWithUnit(contentLength);
        }
        return "文件名:" + fileName;
    }

    private void delete(final String url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.multi_download_dialog, null);
        final CheckBox cbDeleteFile = (CheckBox) view.findViewById(R.id.cbDeleteFile);

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(null)
                .setView(view)
                .setMessage("是否删除所有下载任务")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleteFile = cbDeleteFile.isChecked();
                        Downloader d = JDownloaderManager.getInstance(mContext).delete(url, deleteFile);
                        mList.remove(d);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = alert.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void setData(Map<String, Downloader> map) {
        map2List(map);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged2() {
        map2List(JDownloaderManager.getInstance(mContext).getDownloaderMap());
        notifyDataSetChanged();
    }

    public void notifyItemChanged(String url) {
        Downloader d = JDownloaderManager.getInstance(mContext).getDownloader(url);
        if (d == null) {
            return;
        }
        int position = mList.indexOf(d);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    private void map2List(Map<String, Downloader> map) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.clear();
        if (map == null) {
            return;
        }

        //进行排序后赋值给mList
        Map<String, Downloader> temp = Utils.sortByValue(map);

        for (Map.Entry<String, Downloader> entry : temp.entrySet()) {
            mList.add(entry.getValue());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar pb;
        private TextView tvStatus;
        private TextView tvInfo;
        private Button btnDownload;
        private Button btnCancel;
        private Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            pb = (ProgressBar) itemView.findViewById(R.id.pb);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            btnDownload = (Button) itemView.findViewById(R.id.btnDownload);
            btnCancel = (Button) itemView.findViewById(R.id.btnCancel);
            btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
        }
    }
}
