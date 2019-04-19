package cn.woochen.share

import android.app.Activity
import java.lang.IllegalArgumentException

/**
 *分享工具类
 *@author woochen
 *@time 2019/4/19 13:52
 */
object ShareManager {

    private var mInstance: IShareChannel? = null
        get() = if (field == null) throw IllegalArgumentException("please init ShareManager first!!!") else field

    /**
     * @param shareTypes 分享的类型[ShareType]
     */
    fun init(shareChannel: IShareChannel, vararg shareTypes: ShareType) {
        mInstance = shareChannel
        mInstance?.setShareTypes(shareTypes)
    }

    fun shareText(activity: Activity) {
        mInstance?.shareWithText(activity)
    }

}