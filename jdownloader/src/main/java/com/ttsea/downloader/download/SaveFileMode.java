package com.ttsea.downloader.download;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 保存文件命名方式 <br>
 * <p>
 * <b>more:</b>更多请点 <a href="http://www.ttsea.com" target="_blank">这里</a> <br>
 * <b>date:</b> 2017/2/9 17:37 <br>
 * <b>author:</b> Jason <br>
 * <b>version:</b> 1.0 <br>
 * <b>last modified date:</b> 2017/2/9 17:37.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        SaveFileMode.NONACTION, SaveFileMode.OVERRIDE,
        SaveFileMode.RENAME
})
public @interface SaveFileMode {
    /** 无作为，即不进行下载 */
    int NONACTION = 0;
    /** 覆盖，即将原有的文件覆盖 */
    int OVERRIDE = 1;
    /** 重命名（默认方式），即在文件的后面加上"（2）"等等 */
    int RENAME = 2;
}