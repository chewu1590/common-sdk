package cn.woochen.commonsdk.samples.share

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.woochen.commonsdk.R
import cn.woochen.share.ShareManager
import com.umeng.socialize.Config.shareType
import com.umeng.socialize.ShareAction
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.activity_share.*

/**
 *分享演示类
 *@author woochen
 *@time 2019/4/19 13:47
 */
class ShareActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v) {
            btn_share -> {
                ShareManager.shareText(this)
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        initListener()
    }

    private fun initListener() {
        btn_share.setOnClickListener(this)
    }
}
