package cn.woochen.share.um

import android.app.Activity
import cn.woochen.share.IShareChannel
import cn.woochen.share.ShareType
import com.umeng.socialize.ShareAction
import com.umeng.socialize.bean.SHARE_MEDIA
import java.lang.IllegalArgumentException

/**
 *友盟分享实例
 *@author woochen
 *@time 2019/4/19 13:59
 */
class UmShareChannelImpl :IShareChannel {
    private lateinit var shareType:Array<SHARE_MEDIA?>

    override fun setShareTypes(shareTypes: Array<out ShareType>) {
        if (shareTypes.isEmpty()){
            throw IllegalArgumentException("please add share type!!!")
        }
        shareType = arrayOfNulls(shareTypes.size)
        for (type in shareTypes.withIndex()){
            var shareMEDIA:SHARE_MEDIA?=null
            when (type.value) {
                ShareType.WX -> {
                    shareMEDIA = SHARE_MEDIA.WEIXIN
                }
                ShareType.WX_CIRCLE -> {
                    shareMEDIA = SHARE_MEDIA.WEIXIN_CIRCLE
                }
                else -> {
                }
            }

            if (shareMEDIA != null) {
                shareType[type.index] = shareMEDIA
            }
        }

    }


    override fun shareWithText(activity: Activity) {
        ShareAction(activity).withText("hello")
            .setDisplayList(*shareType)
            .open()
    }


}