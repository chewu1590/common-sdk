package cn.woochen.common_sdk.updater.dialog

import androidx.annotation.StyleRes
import android.text.TextUtils

/**
 * 描述 对话框的配置信息
 * 创建人 kelin
 * 创建时间 2017/7/5  下午4:35
 * 版本 v 1.0.0
 */

abstract class DialogParams {

    @StyleRes
    @get:StyleRes
    var style: Int = 0

    open var title: CharSequence? = null

    /**
     * 是否强制更新。
     *
     * @return true表示强制更新，false则不是。
     */
    var isForceUpdate: Boolean = false

    /**
     * 获取对话框的内容。
     *
     * @return 返回要显示的内容。
     */
    open var content: CharSequence? = null

    var apkSize: Double = 0.0

}
