package cn.woochen.common_sdk.updater.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import cn.woochen.common_sdk.R
import cn.woochen.common_sdk.updater.Updater
import java.util.Locale

/**
 * 描述 更新信息对话框。
 * 创建人 kelin
 * 创建时间 2017/7/5  下午3:59
 * 版本 v 1.0.0
 */

class DefaultDialog(private val mContext: Context) {
    private var mDialog: AlertDialog? = null
    private var mNetWorkUnusableDialog: AlertDialog? = null
    private var mWiFiUnusableDialog: AlertDialog? = null
    private var mProgressBar: ProgressBar? = null
    private var mPercentageView: TextView? = null
    private var mOnClickListener: DialogClickListener? = null
    private var mConfig: DialogParams? = null
    //为了让进度条走完才销毁。
    private val mAction = Runnable { dismiss(mDialog) }

    /**
     * 显示对话框。
     */
    fun show(config: DialogParams) {
        show(null, config, null)
    }

    /**
     * 显示对话框。
     *
     * @param updater
     * @param listener 对话框的监听。
     */
    @SuppressLint("InflateParams")
    fun show(updater: Updater?, config: DialogParams, listener: DialogListener?) {
        dismiss(mWiFiUnusableDialog)
        dismiss(mNetWorkUnusableDialog)
        if (mDialog == null || config !== mConfig) {//没有弹窗或者不是上一个弹窗
            mConfig = config
            //构建AlertDialog
            var builder: AlertDialog.Builder? = null
            if (config is DownloadDialogParams) {//下载弹框
                builder = AlertDialog.Builder(mContext, R.style.dialog)
                val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_download, null)
                mProgressBar = contentView.findViewById(R.id.progress)
                val tv_message = contentView.findViewById<TextView>(R.id.tv_message)
                val tv_size = contentView.findViewById<TextView>(R.id.tv_size)
                val tv_cancel = contentView.findViewById<TextView>(R.id.tv_cancel)
                mPercentageView = contentView.findViewById(R.id.tv_percentage)
                tv_message.text = config.title
                tv_size.text = "本次下载" + config.apkSize + "MB"
                tv_cancel.setOnClickListener {
                    updater?.endDownloadTask()
                }
                builder.setView(contentView)
                builder.setCancelable(false)
                mDialog = builder.create()
            } else if (config is InformDialogParams) { //升级提示弹框
                builder = AlertDialog.Builder(mContext, R.style.dialog)
                val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_update_apk, null)
                val tv_version_code = contentView.findViewById<TextView>(R.id.tv_version_code)
                val tv_confirm = contentView.findViewById<TextView>(R.id.tv_confirm)
                tv_confirm.text = "立即更新"
                val tv_cancel = contentView.findViewById<TextView>(R.id.tv_cancel)
                val view_divider = contentView.findViewById<View>(R.id.view_divider)
                tv_cancel.text = "下次再说"
                if (listener != null) {
                    tv_confirm.setOnClickListener {
                        listener.onDialogDismiss(true)
                        mDialog!!.dismiss()
                    }
                    tv_cancel.setOnClickListener {
                        listener.onDialogDismiss(false)
                        mDialog!!.dismiss()
                    }
                }
                val tv_content = contentView.findViewById<TextView>(R.id.tv_content)
                tv_content.text = config.content.toString()
                if (config.isForceUpdate) {
                    view_divider.visibility = View.GONE
                    tv_cancel.visibility = View.GONE
                }
                builder.setView(contentView)
            }
            builder!!.setCancelable(false)
            mDialog = builder.create()
        }
        mDialog!!.show()
        val window = mDialog!!.window
        val layoutParams = window!!.attributes
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
    }

    /**
     * 更新进度条进度
     *
     * @param percentage
     */
    fun updateDownLoadsProgress(percentage: Int) {
        mProgressBar!!.progress = percentage
        mPercentageView!!.text = String.format(Locale.CHINA, "%d %%", percentage)
        if (percentage == mProgressBar!!.max) {
            mProgressBar!!.post(mAction)
        }
    }

    /**
     * 显示不是wifi环境弹框
     *
     * @param listener
     */
    fun showWiFiUnusableDialog(listener: DialogListener?) {
        dismiss(mNetWorkUnusableDialog)
        dismiss(mDialog)

        if (listener != null && mOnClickListener == null) {
            mOnClickListener = DialogClickListener()
        }
        mOnClickListener!!.setListener(listener)
        if (mWiFiUnusableDialog == null) {
            mWiFiUnusableDialog = AlertDialog.Builder(mContext, mConfig?.style!!)
                .setCancelable(false)
                .setTitle("提示：")
                .setMessage("当前为非WiFi网络，是否继续下载？")
                .setPositiveButton("继续下载", mOnClickListener)
                .setNegativeButton("稍后下载", mOnClickListener)
                .create()
        }
        mWiFiUnusableDialog!!.show()
    }


    fun dismissAll() {
        dismiss(mDialog)
        dismiss(mNetWorkUnusableDialog)
        dismiss(mWiFiUnusableDialog)
    }

    private fun dismiss(dialog: AlertDialog?) {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * 显示网络断开弹框
     */
    fun showNetWorkUnusableDialog(listener: DialogListener?) {
        dismiss(mWiFiUnusableDialog)
        dismiss(mDialog)

        if (listener != null && mOnClickListener == null) {
            mOnClickListener = DialogClickListener()
        }

        mOnClickListener!!.setListener(listener)
        if (mNetWorkUnusableDialog == null) {
            mNetWorkUnusableDialog = AlertDialog.Builder(mContext, mConfig?.style!!)
                .setCancelable(false)
                .setTitle("提示：")
                .setMessage("网络连接已经断开，请稍后再试。")
                .setNegativeButton("确定", mOnClickListener)
                .create()
        }
        mNetWorkUnusableDialog!!.show()
    }

    private inner class DialogClickListener : DialogInterface.OnClickListener {
        private var mListener: DialogListener? = null

        internal fun setListener(listener: DialogListener?) {
            mListener = listener
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            mListener!!.onDialogDismiss(which == DialogInterface.BUTTON_POSITIVE)
        }
    }

    interface DialogListener {

        /**
         * 当用户点击了取消按钮,或通过其他方式销毁了[DefaultDialog]后回调的方法。
         *
         * @param isSure 是否是通过点击确认按钮后销毁的。`true`表示是,
         * `false`则表示不是。
         */
        fun onDialogDismiss(isSure: Boolean)
    }
}
