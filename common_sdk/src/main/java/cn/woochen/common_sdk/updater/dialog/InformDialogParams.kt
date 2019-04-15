package cn.woochen.common_sdk.updater.dialog

import android.text.TextUtils

/**
 * 描述 检查更新时对话框的配置信息。
 * 创建人 kelin
 * 创建时间 2017/7/5  下午4:51
 * 版本 v 1.0.0
 */

class InformDialogParams : DialogParams() {


    /**
     * 获取对话框的标题。
     *
     * @return 返回你要设置的标题。
     */
    override var title: CharSequence? = null
        get() = if (TextUtils.isEmpty(field)) "检测到新的版本" else field

    /**
     * 获取对话框的内容。
     *
     * @return 返回要显示的内容。
     */
    override var content: CharSequence? = null
        get() = if (TextUtils.isEmpty(field)) "是否现在更新？" else field

}
