package com.ttsea.downloader.db;

import android.database.sqlite.SQLiteDatabase;

import com.ttsea.downloader.download.JDownloadLog;


/**
 * 数据库用于保存下载信息<br>
 * Created by Jason on 2015/12/31.
 */
class DBConstants {

    private static final String TAG = "DBConstants";

    /**
     * 只需要在创建数据库的时候调用DBConstants.createTables();<br/>
     * 否则可能不支持一些下载功能
     *
     * @param db SQLiteDatabase
     */
    public static void createTables(SQLiteDatabase db) {
        String[] sqls = new String[]{
                createDOWNLOADINFOTABLE()
        };

        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[0];
            JDownloadLog.d(TAG, "begin create table" + i + ", sql=" + sql);
            db.execSQL(sql);
            JDownloadLog.d(TAG, "end create table");
        }
    }

    public static class TableNames {
        /** 多线程断点下载信息表 */
        public static String DOWNLOAD_INFO = "jdownload_info";
    }

    public static class BaseColumn {
        /** ID，主键，自增列 */
        public static final String COLUMN_ID = "_id";

        /** 扩展1 */
        public static final String COLUMN_EXPAND1 = "expand1";

        /** 扩展2 */
        public static final String COLUMN_EXPAND2 = "expand2";
    }

    public static class DOWNLOAD_INFO_COLUMN extends BaseColumn {

        /** 1.每个线程的id */
        public static final String COLUMN_THREAD_ID = "thread_id";

        /** 2.下载地址 */
        public static final String COLUMN_URL = "url";

        /** 3.本地存储路径 */
        public static final String COLUMN_SAVE_FILE_PATH = "save_file_path";

        /** 4.本地存储的文件名 */
        public static final String COLUMN_FILE_NAME = "file_name";

        /** 5.下载信息描述，可以用来显示在通知栏里 */
        public static final String COLUMN_DESCRIPTION = "description";

        /** 6.添加时的时间 */
        public static final String COLUMN_ADD_TIMESTAMP = "add_timestamp";

        /** 7.最后修改时间 */
        public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";

        /** 8.下载文件的类型 */
        public static final String COLUMN_MEDIA_TYPE = "media_type";

        /** 9.提供下载失败原因 */
        public static final String COLUMN_REASON = "reason";

        /** 10.当前下载的状态 */
        public static final String COLUMN_STATE = "state";

        /** 11.文件总长度 */
        public static final String COLUMN_CONTENT_LENGTH = "content_length";

        /** 12.开始下载的点 */
        public static final String COLUMN_START_BYTES = "start_bytes";

        /** 13.需要读取的长度 */
        public static final String COLUMN_NEED_READ_LENGTH = "need_read_length";

        /** 14.已经读取了的长度 */
        public static final String COLUMN_HAS_READ_LENGTH = "has_read_length";

        /** 15.Etag */
        public static final String COLUMN_ETAG = "etag";

        /** 16.文件md5 */
        public static final String COLUMN_FILE_MD5 = "file_md5";

    }

    private static String createDOWNLOADINFOTABLE() {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE IF NOT EXISTS ")
                .append(TableNames.DOWNLOAD_INFO).append(" ( ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_THREAD_ID)// 1.线程id
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_URL)//2.下载地址
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_SAVE_FILE_PATH)//3.本地存储路径
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_FILE_NAME)//4.本地存储的文件名
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_DESCRIPTION)//5.描述
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_ADD_TIMESTAMP)//6.添加时间
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_LAST_MODIFIED_TIMESTAMP)//7.最后修改时间
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_MEDIA_TYPE)//8.文件类型
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_REASON)//9.下载失败原因
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_STATE)//10.当前状态
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_CONTENT_LENGTH)//11.文件大小
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_START_BYTES)// 12.开始下载的点
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_NEED_READ_LENGTH)// 13.需要读取的长度
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_HAS_READ_LENGTH)//14.已经读取了的长度
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_ETAG)//15.ETag
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_FILE_MD5)//16.文件md5
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_EXPAND1)
                .append(" TEXT, ")
                .append(DOWNLOAD_INFO_COLUMN.COLUMN_EXPAND2)
                .append(" TEXT")
                .append(" );");

        return sb.toString();
    }
}
