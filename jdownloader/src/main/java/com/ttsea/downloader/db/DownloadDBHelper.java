package com.ttsea.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ttsea.downloader.download.DownloaderInfo;
import com.ttsea.downloader.download.JDownloadLog;

import java.util.ArrayList;
import java.util.List;


/**
 * 下载数据表操作
 * Created by Jason on 2016/1/5.
 */
public class DownloadDBHelper {
    private final static String TAG = "DownloadDBHelper";
    private final static String DOWNLOAD_INFO = DBConstants.TableNames.DOWNLOAD_INFO;

    public static final String THREAD_ID = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_THREAD_ID;
    public static final String _URL = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_URL;
    public static final String SAVE_FILE_PATH = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_SAVE_FILE_PATH;
    public static final String FILE_NAME = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_FILE_NAME;
    public static final String DESCRIPTION = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_DESCRIPTION;
    public static final String ADD_TIMESTAMP = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_ADD_TIMESTAMP;
    public static final String LAST_MODIFIED_TIMESTAMP = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_LAST_MODIFIED_TIMESTAMP;
    public static final String MEDIA_TYPE = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_MEDIA_TYPE;
    public static final String REASON = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_REASON;
    public static final String STATE = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_STATE;
    public static final String CONTENT_LENGTH = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_CONTENT_LENGTH;
    public static final String START_BYTES = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_START_BYTES;
    public static final String NEED_READ_LENGTH = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_NEED_READ_LENGTH;
    public static final String HAS_READ_LENGTH = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_HAS_READ_LENGTH;
    public static final String ETAG = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_ETAG;
    public static final String FILE_MD5 = DBConstants.DOWNLOAD_INFO_COLUMN.COLUMN_FILE_MD5;

    /**
     * 获取下载游标，但url为null的时，则threadId无效，会直接返回所有的Cursor
     *
     * @param db  SQLiteDatabase
     * @param url 下载地址，当url为null时，则返回所有的
     * @return Cursor 或者 null
     */
    private synchronized static Cursor getDownloaderInfoCursor(SQLiteDatabase db, String url, String threadId) {
        Cursor cursor = null;
        StringBuffer selection = new StringBuffer();

        if (url != null) {
            selection.append("url = '" + url + "'");

            if (threadId != null) {
                selection.append(" AND thread_id = '" + threadId + "'");
            }
        }

        selection.append(" ORDER BY add_timestamp DESC ");

        try {
            cursor = db.query(DOWNLOAD_INFO, null, selection.toString(), null, null, null, null);

        } catch (Exception e) {
            JDownloadLog.d(TAG, "getDownloaderInfoCursor, Exception e:" + e.toString());
            return null;
        }

        return cursor;
    }

    /**
     * 获取下载器信息，当url为null时，将会获取所有的DownloaderInfo
     *
     * @param context 上下文
     * @param url     下载地址
     * @return DownloadFileInfo of list 或者null
     */
    public synchronized static List<DownloaderInfo> getDownloaderInfos(Context context, String url) {
        List<DownloaderInfo> infos = new ArrayList<DownloaderInfo>();
        Cursor cursor = null;
        SQLiteDatabase db = DBHelper.getReadableDatabase(context);

        try {
            cursor = getDownloaderInfoCursor(db, url, null);

            if (cursor == null || cursor.getCount() < 1) {
                closeCursor(cursor);
                closeDB(db);
                return infos;
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String thread_id = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.THREAD_ID));
                //String url = cursor.getString(cursor.getColumnIndex(DownloadDBHelper._URL));
                String save_file_path = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.SAVE_FILE_PATH));
                String file_name = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.FILE_NAME));
                String description = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.DESCRIPTION));
                String add_timestamp = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.ADD_TIMESTAMP));
                String last_modified_timestamp = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.LAST_MODIFIED_TIMESTAMP));
                String media_type = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.MEDIA_TYPE));
                int reason = cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.REASON));
                int state = cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.STATE));
                long content_length = cursor.getLong(cursor.getColumnIndex(DownloadDBHelper.CONTENT_LENGTH));
                long start_bytes = cursor.getLong(cursor.getColumnIndex(DownloadDBHelper.START_BYTES));
                long need_read_length = cursor.getLong(cursor.getColumnIndex(DownloadDBHelper.NEED_READ_LENGTH));
                long has_read_length = cursor.getLong(cursor.getColumnIndex(DownloadDBHelper.HAS_READ_LENGTH));
                String etag = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.ETAG));
                String file_md5 = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.FILE_MD5));

                DownloaderInfo downloaderInfo = new DownloaderInfo(context, url);
                downloaderInfo.setThreadId(thread_id);
                downloaderInfo.setSaveFilePath(save_file_path);
                downloaderInfo.setFileName(file_name);
                downloaderInfo.setDescription(description);
                downloaderInfo.setAddTimestamp(add_timestamp);
                downloaderInfo.setLastModifiedTimestamp(last_modified_timestamp);
                downloaderInfo.setMediaType(media_type);
                downloaderInfo.setReason(reason);
                downloaderInfo.setState(state);
                downloaderInfo.setContentLength(content_length);
                downloaderInfo.setStartBytes(start_bytes);
                downloaderInfo.setNeedReadLength(need_read_length);
                downloaderInfo.setHasReadLength(has_read_length);
                downloaderInfo.setEtag(etag);
                downloaderInfo.setFileMd5(file_md5);

                JDownloadLog.d(TAG, "add downloadInfo in to infos, downloadInfo:" + downloaderInfo.toString());

                infos.add(downloaderInfo);
            }

        } catch (Exception e) {
            JDownloadLog.d(TAG, "getThreadPool, Exception e:" + e.toString());
            return infos;

        } finally {
            closeCursor(cursor);
            closeDB(db);
        }

        return infos;
    }

    /**
     * 通过url和thread拿到指定的DownloaderInfo
     *
     * @param context 上下文
     * @param url     下载地址
     * @param thread  下载线程id
     * @return DownloaderInfo or null
     */
    public synchronized static DownloaderInfo getDownloaderInfo(Context context, String url, String thread) {
        List<DownloaderInfo> infos = getDownloaderInfos(context, url);
        for (int i = 0; i < infos.size(); i++) {
            if (infos.get(i).getThreadId() != null && infos.get(i).getThreadId().equals(thread)) {
                return infos.get(i);
            }
        }
        return null;
    }

    public synchronized static long insertOrUpdate(Context context, DownloaderInfo info) {
        if (info == null) {
            JDownloadLog.d(TAG, "insertOrUpdate, info is null");
            return 0;
        }

        SQLiteDatabase db = DBHelper.getWritableDatabase(context);
        Cursor cursor = getDownloaderInfoCursor(db, info.getUrl(), info.getThreadId());

        if (cursor != null && cursor.getCount() > 0) {
            closeCursor(cursor);
            closeDB(db);
            return update(context, info);
        }

        long count = 0;

        ContentValues values = new ContentValues();
        values.put(THREAD_ID, info.getThreadId());
        values.put(_URL, info.getUrl());
        values.put(SAVE_FILE_PATH, info.getSaveFilePath());
        values.put(FILE_NAME, info.getFileName());
        values.put(DESCRIPTION, info.getDescription());
        values.put(ADD_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        values.put(LAST_MODIFIED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        values.put(MEDIA_TYPE, info.getMediaType());
        values.put(REASON, info.getReason());
        values.put(STATE, info.getState());
        values.put(CONTENT_LENGTH, info.getContentLength());
        values.put(START_BYTES, info.getStartBytes());
        values.put(NEED_READ_LENGTH, info.getNeedReadLength());
        values.put(HAS_READ_LENGTH, info.getHasReadLength());
        values.put(ETAG, info.getEtag());
        values.put(FILE_MD5, info.getFileMd5());

        try {
            count = db.insertOrThrow(DOWNLOAD_INFO, null, values);

        } catch (Exception e) {
            JDownloadLog.d(TAG, "add, Exception e:" + e.toString());

        } finally {
            closeCursor(cursor);
            closeDB(db);
        }
        JDownloadLog.d(TAG, "insertOrUpdate, threadId:" + info.getThreadId() + ", count:" + count);

        return count;
    }

    public synchronized static long update(Context context, DownloaderInfo info) {
        if (info == null) {
            JDownloadLog.d(TAG, "insertOrUpdate, info is null");
            return 0;
        }

        SQLiteDatabase db = DBHelper.getWritableDatabase(context);
        long count = 0;
        String whereClause = "url='" + info.getUrl() + "' AND thread_id='"
                + info.getThreadId() + "'";

        ContentValues values = new ContentValues();
        //values.put(THREAD_ID, info.getThreadId());
        //values.put(_URL, info.getUrl());
        values.put(SAVE_FILE_PATH, info.getSaveFilePath());
        values.put(FILE_NAME, info.getFileName());
        values.put(DESCRIPTION, info.getDescription());
        //values.put(ADD_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        values.put(LAST_MODIFIED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        values.put(MEDIA_TYPE, info.getMediaType());
        values.put(REASON, info.getReason());
        values.put(STATE, info.getState());
        values.put(CONTENT_LENGTH, info.getContentLength());
        values.put(START_BYTES, info.getStartBytes());
        values.put(NEED_READ_LENGTH, info.getNeedReadLength());
        values.put(HAS_READ_LENGTH, info.getHasReadLength());
        values.put(ETAG, info.getEtag());
        values.put(FILE_MD5, info.getFileMd5());

        try {
            count = db.update(DOWNLOAD_INFO, values, whereClause, null);

        } catch (Exception e) {
            JDownloadLog.e(TAG, "update, Exception e:" + e.toString());

        } finally {
            closeDB(db);
        }
        JDownloadLog.d(TAG, "update, threadId:" + info.getThreadId() + ", count:" + count);

        return count;
    }

    /**
     * 下载记录是否已经存在
     *
     * @param context 上下文
     * @param url     url
     * @return 存在返回true，不存在返回false
     */
    public synchronized static boolean isDownloaderExist(Context context, String url) {
        SQLiteDatabase db = DBHelper.getWritableDatabase(context);

        Cursor cursor = getDownloaderInfoCursor(db, url, null);
        if (cursor == null || cursor.getCount() < 1) {
            closeCursor(cursor);
            closeDB(db);
            return false;
        }

        closeCursor(cursor);
        closeDB(db);
        return true;
    }

    /**
     * 删除下载记录
     *
     * @param context 上下文
     * @param url     下载地址
     * @return 返回删除的数量
     */
    public synchronized static long deleteRecord(Context context, String url) {

        int count = 0;

        String whereClause = "url='" + url + "'";
        if (url == null || url.length() < 1) {
            whereClause = null;
        }
        SQLiteDatabase db = DBHelper.getWritableDatabase(context);

        try {
            count = db.delete(DOWNLOAD_INFO, whereClause, null);

        } catch (Exception e) {
            JDownloadLog.e(TAG, "update, Exception e:" + e.toString());
            count = 0;

        } finally {
            closeDB(db);
        }

        return count;
    }

    /**
     * 删除下载记录
     *
     * @param context 上下文
     * @return 返回删除的数量
     */
    public synchronized static long deleteAllRecord(Context context) {
        return deleteRecord(context, null);
    }

    private synchronized static void closeDB(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private synchronized static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    private synchronized static void printCursor(Cursor c) {
        if (c == null) {
            return;
        }

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int columnCount = c.getColumnCount();
            String columnInfo = "";
            for (int i = 0; i < columnCount; i++) {
                columnInfo = columnInfo + "columnName:" + c.getColumnName(i) + "-columnValue:" + c.getString(i) + ", ";
            }
            JDownloadLog.d(TAG, columnInfo);
        }
    }
}