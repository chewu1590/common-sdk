package cn.woochen.commonsdk

import android.app.Application
import cn.woochen.share.ShareManager
import cn.woochen.share.ShareType
import cn.woochen.share.um.BaseWXShareActivity
import cn.woochen.share.um.UmShareChannelImpl
import cn.woochen.share_annotation.WXShareEntry
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig

@WXShareEntry(packageName = "cn.woochen.commonsdk",entryClass = BaseWXShareActivity::class)
class AppAplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initShare()
    }

    private fun initShare() {
        UMConfigure.setLogEnabled(true)
        UMConfigure.init(this, "xxxxxxxxxxxxxxx", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "")//需要配置
        PlatformConfig.setWeixin("wxe9xxxxxx", "xxxxxxxxxxxxxxxxxx")//需要配置
        ShareManager.init(UmShareChannelImpl(),ShareType.WX,ShareType.WX_CIRCLE)
    }
}