package cn.woochen.commonsdk.samples.updater

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.TextView
import cn.woochen.common_sdk.updater.UpdateInfo
import cn.woochen.common_sdk.updater.Updater
import cn.woochen.common_sdk.updater.callback.DialogEventCallback
import cn.woochen.common_sdk.updater.callback.UpdateCallback
import cn.woochen.commonsdk.R
import cn.woochen.commonsdk.samples.bean.UpdateVersionBean
import kotlinx.android.synthetic.main.activity_updater.*

/**
 *版本检测更新演示类
 *@author woochen
 *@time 2019/4/15 14:42
 */
class UpdaterActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var downloadDialog: AlertDialog

    override fun onClick(v: View?) {
        when (v) {
            btn_simple -> {
                simpleUpdate()
            }
            btn_custom -> {
                customUIUpdate()
            }
        }
    }

    private fun customUIUpdate() {
        val updateVersionBean = UpdateVersionBean()
        updateVersionBean.versionName
        val updater = Updater.Builder(this)
            .setCallback(object : UpdateCallback() {
                override fun onCompleted(haveNewVersion: Boolean, curVersionName: String) {
                    super.onCompleted(haveNewVersion, curVersionName)
                    Log.e("eee", "更新结束")
                }
            })
            .setCheckWiFiState(true)
            .setCustomCheckDialog(object : DialogEventCallback {
                override fun onShowCheckHintDialog(updater: Updater, updateInfo: UpdateInfo, isForce: Boolean) {
                    AlertDialog.Builder(this@UpdaterActivity).setMessage("检测到新版本，是否立即更新？")
                        .setPositiveButton("确定") { dialog, _ ->
                            updater.setCheckHandlerResult(true)
                            dialog.dismiss() }
                        .show()
                }

                override fun onShowProgressDialog(isForce: Boolean) {
                    downloadDialog = AlertDialog.Builder(this@UpdaterActivity)
                        .setTitle("正在下载")
                        .setMessage("已下载0%")
                        .setNegativeButton("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    downloadDialog.show()
                }

                override fun onProgress(total: Long, current: Long, percentage: Int) {
                    downloadDialog.setMessage("已经下载$percentage%")
                    if (current == total) downloadDialog.dismiss()
                }

            })
            .builder()
        updater.check(updateVersionBean)
    }

    private fun simpleUpdate() {
        val updateVersionBean = UpdateVersionBean()
        val updater = Updater.Builder(this)
            .setCallback(object : UpdateCallback() {
                override fun onCompleted(haveNewVersion: Boolean, curVersionName: String) {
                    super.onCompleted(haveNewVersion, curVersionName)
                    Log.e("eee", "更新结束")
                }
            })
            .setCheckWiFiState(true)
            .builder()
        updater.check(updateVersionBean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)
        initView()
    }

    private fun initView() {
        btn_simple.setOnClickListener(this)
        btn_custom.setOnClickListener(this)
    }
}
