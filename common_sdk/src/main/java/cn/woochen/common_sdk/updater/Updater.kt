package cn.woochen.common_sdk.updater

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.IBinder
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.widget.Toast
import cn.woochen.common_sdk.updater.callback.DialogEventCallback
import cn.woochen.common_sdk.updater.callback.DownloadProgressCallback
import cn.woochen.common_sdk.updater.callback.UpdateCallback
import cn.woochen.common_sdk.updater.dialog.DefaultDialog
import cn.woochen.common_sdk.updater.dialog.DownloadDialogParams
import cn.woochen.common_sdk.updater.dialog.InformDialogParams
import cn.woochen.common_sdk.updater.util.NetWorkStateUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 描述 用来更新APK的核心类。
 * 创建人 kelin
 * 创建时间 2017/3/13  下午2:32
 * 版本 v 1.0.0
 */
class Updater
/**
 * 私有构造函数，防止其他类创建本类对象。
 */
private constructor(private val mBuilder: Builder) {
    private val mCallback: UpdateCallback?
    private var isBindService: Boolean = false
    private var conn: ServiceConnection? = null
    private var mServiceIntent: Intent? = null
    private val mDefaultDialog: DefaultDialog?
    private var mIsLoaded: Boolean = false
    private var mUpdateInfo: UpdateInfo? = null
    private var mHaveNewVersion: Boolean = false
    private var mIsChecked: Boolean = false
    private var mAutoInstall = true
    private var mNetWorkStateChangedReceiver: NetWorkStateChangedReceiver? = null
    private var mOnProgressListener: OnLoadProgressListener? = null
    private var mIsProgressDialogHidden: Boolean = false
    private val mDialogListener = DefaultDialogListener()
    private val mApplicationContext: Context

    private//接口回调，下载进度
    val serviceConnection: ServiceConnection
        get() {
            if (conn == null) {
                conn = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
                        val binder = service as DownloadService.DownloadBinder
                        val downloadService = binder.service
                        if (mOnProgressListener == null) {
                            mOnProgressListener = OnLoadProgressListener()
                        }
                        downloadService.setOnProgressListener(mOnProgressListener!!)

                        downloadService.setServiceUnBindListener(object : DownloadService.ServiceUnBindListener {
                            override fun onUnBind() {
                                isBindService = false
                            }
                        })
                    }

                    override fun onServiceDisconnected(name: ComponentName) {

                    }
                }
            }
            return conn!!
        }

    /**
     * 获取默认的Apk名称。
     *
     * @return 返回一个以 "包名+日期" 命名的Apk名称。
     */
    private val defaultApkName: String
        get() {
            val format = SimpleDateFormat("yyyy-M-d_HH-MM", Locale.CHINA)
            val formatDate = format.format(Date())
            return mApplicationContext.packageName + formatDate + ".apk"
        }

    init {
        mCallback = mBuilder.callback
        mApplicationContext = mBuilder.context.applicationContext
        mDefaultDialog = DefaultDialog(mBuilder.context)
    }


    private fun registerNetWorkReceiver() {
        if (mNetWorkStateChangedReceiver == null) {
            mNetWorkStateChangedReceiver = NetWorkStateChangedReceiver()
        }
        if (mNetWorkStateChangedReceiver?.isRegister!!) {
            NetWorkStateUtil.registerReceiver(mApplicationContext, mNetWorkStateChangedReceiver!!)
        }
    }

    private fun unregisterNetWorkReceiver() {
        if (mNetWorkStateChangedReceiver != null) {
            NetWorkStateUtil.unregisterReceiver(mApplicationContext, mNetWorkStateChangedReceiver)
        }
    }

    /**
     * 判断当前版本是否是强制更新。
     *
     * @return 如果是返回true，否则返回false。
     */
    private fun isForceUpdate(updateInfo: UpdateInfo): Boolean {
        return isForceUpdate(updateInfo, mApplicationContext)
    }

    /**
     * 显示进度条对话框。
     */
    private fun showProgressDialog() {
        if (!mBuilder.noDialog) {
            mDefaultDialog?.show(
                this@Updater,
                mBuilder.loadDialogConfig,
                mDialogListener.changeState(mDialogListener.STATE_DOWNLOAD)
            )
        } else {
            if (mBuilder.dialogCallback != null) {
                mDefaultDialog?.dismissAll()
                mBuilder.dialogCallback!!.onShowProgressDialog(isForceUpdate(mUpdateInfo!!))
            } else {
                throw IllegalArgumentException("you mast call Updater's \"setCallback(UpdateCallback callback)\" Method。")
            }
        }
    }

    /**
     * 更新进度条对话框进度。
     *
     * @param percentage 当前的百分比。
     */
    private fun updateProgressDialog(percentage: Int) {
        mDefaultDialog?.updateDownLoadsProgress(percentage)
    }

    fun stopService() {
        //判断是否真的下载完成进行安装了，以及是否注册绑定过服务
        if (isBindService) {
            mApplicationContext.unbindService(conn!!)
            isBindService = false
        }
        mApplicationContext.stopService(mServiceIntent)
    }

    /**
     * 检查更新
     *
     * @param updateInfo  更新信息对象。
     * @param autoInstall 是否自动安装，true表示在下载完成后自动安装，false表示不需要安装。
     */
    @JvmOverloads
    fun check(updateInfo: UpdateInfo?, autoInstall: Boolean = true) {
        if (!autoInstall && mCallback == null) {
            throw IllegalArgumentException("Because you neither set up to monitor installed automatically, so the check update is pointless.")
        }
        if (!NetWorkStateUtil.isConnected(mApplicationContext)) {//网络不可用
            mCallback?.onCompleted(false, UpdateHelper.getCurrentVersionName(mApplicationContext))
            return
        }
        if (updateInfo != null && mUpdateInfo !== updateInfo) {
            if (TextUtils.isEmpty(updateInfo.downLoadsUrl)) {//没有下载地址直接失败
                mCallback?.onLoadFailed()
                mCallback?.onCompleted(
                    !TextUtils.equals(updateInfo.versionName, getLocalVersionName(mApplicationContext)),
                    getLocalVersionName(mApplicationContext)
                )
                return
            }
            mAutoInstall = autoInstall
            mIsChecked = true
            mUpdateInfo = updateInfo
            mBuilder.loadDialogConfig.isForceUpdate = isForceUpdate(updateInfo)//配置下载弹窗(是否能后台下载)
            mBuilder.loadDialogConfig.apkSize = updateInfo.apkSize!!//apk大小
            mBuilder.informDialogConfig.content = updateInfo.updateMessage//配置版本检测弹窗(更新信息)
            mBuilder.informDialogConfig.apkVersion = updateInfo.versionName//配置版本检测弹窗(更新信息)
            //如果这个条件满足说明上一次没有安装。有因为即使上一次没有安装最新的版本也有可能超出了上一次下载的版本，所以要在这里判断。
            val apkPath: String? = UpdateHelper.getApkPathFromSp(mApplicationContext)
            if (TextUtils.equals(UpdateHelper.getApkVersionNameFromSp(mApplicationContext),updateInfo.versionName)
                && (apkPath?.toLowerCase()?.endsWith(".apk")!!)
                && File(apkPath).exists()
            ) {//已经下载过
                mIsLoaded = true
            } else {
                UpdateHelper.removeOldApk(mApplicationContext)
            }
            if (!TextUtils.equals(updateInfo.versionName, getLocalVersionName(mApplicationContext))) {
                mHaveNewVersion = true
                if (!mBuilder.noDialog) {
                    mBuilder.informDialogConfig.isForceUpdate = isForceUpdate(updateInfo)//配置版本检测弹窗(是否可以忽略更新)
                    showUpdateInformDialog()//显示版本更新弹框
                } else {
                    if (mBuilder.dialogCallback != null) {//当初始化的时候没有配置更新信息对话框时，在这里生成对话框
                        mBuilder.dialogCallback!!.onShowCheckHintDialog(
                            this@Updater,
                            updateInfo,
                            isForceUpdate(updateInfo)
                        )
                    } else {
                        throw IllegalArgumentException("you must call Updater's \"setCallback(UpdateCallback callback)\" Method。")
                    }
                }
            } else {
                //没有新版本
                mCallback?.onCompleted(false, UpdateHelper.getCurrentVersionName(mApplicationContext))
            }
        }
    }

    /**
     * 显示更新提醒。
     */
    private fun showUpdateInformDialog() {

        mDefaultDialog?.show(
            this@Updater,
            mBuilder.informDialogConfig,
            mDialogListener.changeState(mDialogListener.STATE_CHECK_UPDATE)
        )
    }

    /**
     * 设置检查更新的对话框的操作结果。如果你没有关闭默认的对话框使用自定义对话框的话请不要手动调用该方法。
     *
     * @param isContinue 是否继续，如果继续则说明统一更新，否则就是不统一更新。
     */
    fun setCheckHandlerResult(isContinue: Boolean) {
        if (!mBuilder.noDialog || !mIsChecked) {  //如果不是自定义UI交互或没有使用API提供的check方法检测更新的话不允许调用该方法。
            throw IllegalStateException("Because of your dialog is not custom, so you can't call the method.")
        }
        respondCheckHandlerResult(isContinue)
    }

    /**
     * 响应检查更新的对话框的操作结果。如果你没有关闭默认的对话框使用自定义对话框的话请不要手动调用该方法。
     *
     * @param isContinue 是否继续，如果继续则说明同意更新，否则就是不同意更新。
     */
    private fun respondCheckHandlerResult(isContinue: Boolean) {
        if (isContinue && mHaveNewVersion) {
            val apkFile= File(UpdateHelper.getApkPathFromSp(mApplicationContext))
            if (mIsLoaded && (apkFile.exists())) {
                if (mCallback != null) {
                    mCallback.onLoadSuccess(apkFile, true)
                    mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
                }
                if (mAutoInstall) {
                    val installApk = UpdateHelper.installApk(mApplicationContext, apkFile)
                    if (!installApk && mCallback != null) {
                        mCallback.onInstallFailed()
                    }
                }
            } else {
                if (checkCanDownloadable()) {
                    startDownload()
                }
            }
        } else {
            if (mCallback != null) {
                mCallback.onLoadCancelled()
                mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
            }
        }
    }

    private fun checkCanDownloadable(): Boolean {
        registerNetWorkReceiver()  //注册一个网络状态改变的广播接收者。无论网络是否连接成功都要注册，因为下载过程中可能会断网。
        if (!NetWorkStateUtil.isConnected(mApplicationContext) || mBuilder.checkWiFiState && !NetWorkStateUtil.isWifiConnected(
                mApplicationContext
            )
        ) {
            showWifiOrMobileUnusableDialog()
            return false
        }
        return true
    }

    private fun showWifiOrMobileUnusableDialog() {
        if (NetWorkStateUtil.isConnected(mApplicationContext)) {
            showWiFiUnusableDialog()
        } else {
            showNetWorkUnusableDialog()
        }
    }

    private fun showNetWorkUnusableDialog() {
        mDefaultDialog?.showNetWorkUnusableDialog(mDialogListener.changeState(mDialogListener.STATE_NETWORK_UNUSABLE))
    }

    private fun showWiFiUnusableDialog() {
        mDefaultDialog?.showWiFiUnusableDialog(mDialogListener.changeState(mDialogListener.STATE_WIFI_UNUSABLE))
    }

    private fun getApkName(updateInfo: UpdateInfo): String {
        val apkName = updateInfo.apkName
        return if (TextUtils.isEmpty(apkName)) {
            defaultApkName
        } else {
            if (apkName!!.toLowerCase().endsWith(".apk")) apkName else "$apkName.apk"
        }
    }

    /**
     * 开始下载。
     *
     * @param updateInfo  更新信息对象。
     * @param autoInstall 是否自动安装，true表示在下载完成后自动安装，false表示不需要安装。
     */
    @JvmOverloads
    fun download(updateInfo: UpdateInfo, autoInstall: Boolean = true) {
        download(updateInfo, null, null, autoInstall)
    }

    /**
     * 开始下载。
     *
     * @param updateInfo        更新信息对象。
     * @param notifyCationTitle 下载过程中通知栏的标题。如果是强制更新的话该参数可以为null，因为强制更新没有通知栏提示。
     * @param notifyCationDesc  下载过程中通知栏的描述。如果是强制更新的话该参数可以为null，因为强制更新没有通知栏提示。
     * @param autoInstall       是否自动安装，true表示在下载完成后自动安装，false表示不需要安装。
     */
    fun download(
        updateInfo: UpdateInfo,
        notifyCationTitle: CharSequence?,
        notifyCationDesc: CharSequence?,
        autoInstall: Boolean
    ) {
        if (mIsChecked) {  //如果检查更新不是自己检查的就不能调用这个方法。
            throw IllegalStateException("Because you update the action is completed, so you can't call this method.")
        }
        if (!autoInstall && mCallback == null) {
            throw IllegalArgumentException("Because you have neither set up to monitor installed automatically, so the download is pointless.")
        }
        if (TextUtils.isEmpty(updateInfo.downLoadsUrl)) {
            return
        }
        mBuilder.mTitle = if (TextUtils.isEmpty(notifyCationTitle)) "正在下载更新" else notifyCationTitle
        mBuilder.mDescription = notifyCationDesc
        mAutoInstall = autoInstall
        mUpdateInfo = updateInfo
        mBuilder.loadDialogConfig.isForceUpdate = isForceUpdate(updateInfo)
        if (checkCanDownloadable()) {
            startDownload()
        }
    }

    /**
     * 开始下载。
     */
    private fun startDownload() {
        mServiceIntent = Intent(mApplicationContext, DownloadService::class.java)
        mServiceIntent!!.putExtra(DownloadService.KEY_APK_NAME, getApkName(mUpdateInfo!!))
        mServiceIntent!!.putExtra(DownloadService.KEY_DOWNLOAD_URL, mUpdateInfo!!.downLoadsUrl)
        mServiceIntent!!.putExtra(DownloadService.KEY_IS_FORCE_UPDATE, isForceUpdate(mUpdateInfo!!))
        mServiceIntent!!.putExtra(DownloadService.KEY_NOTIFY_TITLE, mBuilder.mTitle)
        mServiceIntent!!.putExtra(DownloadService.KEY_NOTIFY_DESCRIPTION, mBuilder.mDescription)

        mApplicationContext.startService(mServiceIntent)
        isBindService = mApplicationContext.bindService(mServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    class Builder(context: Activity) {

        val context: Context
        val informDialogConfig = InformDialogParams()
        val loadDialogConfig = DownloadDialogParams()
        /**
         * 用来配置下载的监听回调对象。
         */
         lateinit var callback: UpdateCallback
        /**
         * 通知栏的标题。
         */
        internal var mTitle: CharSequence? = null
        /**
         * 通知栏的描述。
         */
        internal var mDescription: CharSequence? = null
        /**
         * 是否没有对话框。
         */
        var noDialog: Boolean = false
        /**
         * 是否检测WiFi链接状态。
         */
        var checkWiFiState = true
        var dialogCallback: DialogEventCallback? = null

        internal var mApkSize: Double = 0.toDouble()

        init {
            this.context = context
        }

        /**
         * 设置监听对象。
         *
         * @param callback 监听回调对象。
         */
        fun setCallback(callback: UpdateCallback): Builder {
            this.callback = callback
            return this
        }

        fun setApkSize(apkSize: Double): Builder {
            this.mApkSize = apkSize
            return this
        }

        /**
         * 设置Dialog的样式。
         *
         * @param style 要设置的样式的资源ID。
         */
        fun setDialogTheme(@StyleRes style: Int): Builder {
            informDialogConfig.style = style
            return this
        }

        /**
         * 配置检查更新时对话框的标题。
         *
         * @param title 对话框的标题。
         */
        fun setCheckDialogTitle(title: CharSequence): Builder {
            informDialogConfig.title = title
            return this
        }

        /**
         * 配置下载更新时对话框的标题。
         *
         * @param title 对话框的标题。
         */
        fun setDownloadDialogTitle(title: CharSequence): Builder {
            loadDialogConfig.title = title
            if (this.mTitle == null) {
                this.mTitle = title
            }
            return this
        }

        /**
         * 配置下载更新时对话框的消息。
         *
         * @param message 对话框的消息。
         */
        fun setDownloadDialogMessage(message: String): Builder {
            loadDialogConfig.content = message
            return this
        }

        /**
         * 设置通知栏的标题。
         */
        fun setNotifyTitle(title: CharSequence): Builder {
            this.mTitle = title
            return this
        }

        /**
         * 设置通知栏的描述。
         */
        fun setNotifyDescription(description: CharSequence): Builder {
            this.mDescription = description
            return this
        }

        /**
         * 如果你希望自己创建对话框，而不使用默认提供的对话框，可以调用该方法将默认的对话框关闭。
         * 如果你关闭了默认的对话框的话就必须自己实现UI交互，并且在用户更新提示做出反应的时候调用
         * [.setCheckHandlerResult] 方法。
         */
        fun setCustomCheckDialog(callback: DialogEventCallback): Builder {
            this.noDialog = true
            this.dialogCallback = callback
            return this
        }

        /**
         * 设置不检查WiFi状态，默认是检查WiFi状态的，也就是说如果在下载更新的时候如果没有链接WiFi的话默认是会提示用户的。
         * 但是如果你不希望给予提示，就可以通过调用此方法，禁用WiFi检查。
         *
         * @param check 是否检测WiFi连接状态，true表示检测，false表示不检测。默认检测。
         */
        fun setCheckWiFiState(check: Boolean): Builder {
            this.checkWiFiState = check
            return this
        }

        /**
         * 构建 [Updater] 对象。
         *
         * @return 返回一个构建好的 [Updater] 对象。
         */
        fun builder(): Updater {
            return Updater(this)
        }
    }

    private inner class NetWorkStateChangedReceiver : NetWorkStateUtil.ConnectivityChangeReceiver() {
        /**
         * 当链接断开的时候执行。
         *
         * @param type 表示当前断开链接的类型，是WiFi还是流量。如果为 [ConnectivityManager.TYPE_WIFI] 则说明当前断开链接
         * 的是WiFi，如果为 [ConnectivityManager.TYPE_MOBILE] 则说明当前断开链接的是流量。
         */
        override fun onDisconnected(type: Int) {
            showNetWorkUnusableDialog()
        }

        /**
         * 当链接成功后执行。
         *
         * @param type 表示当前链接的类型，是WiFi还是流量。如果为 [ConnectivityManager.TYPE_WIFI] 则说明当前链接
         * 成功的是WiFi，如果为 [ConnectivityManager.TYPE_MOBILE] 则说明当前链接成功的是流量。
         */
        override fun onConnected(type: Int) {
            when (type) {
                ConnectivityManager.TYPE_MOBILE -> if (isBindService) {
                    if (!mIsProgressDialogHidden) {
                        showProgressDialog()
                    }
                } else {
                    if (mBuilder.checkWiFiState) {
                        showWiFiUnusableDialog()
                    } else {
                        startDownload()
                    }
                }
                ConnectivityManager.TYPE_WIFI -> if (isBindService) {
                    showProgressDialog()
                } else {
                    startDownload()
                }
            }
        }
    }

    private inner class OnLoadProgressListener : DownloadProgressCallback {

        override fun onStartDownLoad() {
            mCallback?.onStartDownLoad()
            showProgressDialog()
        }

        override fun onProgress(total: Long, current: Long, percentage: Int) {
            if (percentage == 100 || total == current) {
                UpdateHelper.putApkVersionCode2Sp(mApplicationContext, mUpdateInfo?.versionCode!!)
                UpdateHelper.putApkVersionName2Sp(mApplicationContext, mUpdateInfo?.versionName!!)
            }
            mCallback?.onProgress(total, current, percentage)
            if (mBuilder.dialogCallback != null) {
                mBuilder.dialogCallback!!.onProgress(total, current, percentage)
            }
            if (!mBuilder.noDialog) {
                updateProgressDialog(percentage)
            }
        }

        override fun onLoadSuccess(apkFile: File, isCache: Boolean) {
            unregisterNetWorkReceiver()
            stopService()  //结束服务
            if (mCallback != null) {
                mCallback.onLoadSuccess(apkFile, isCache)
                mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
            }
            if (mAutoInstall) {
                UpdateHelper.installApk(mApplicationContext, apkFile)
            }
        }

        override fun onLoadFailed() {
            unregisterNetWorkReceiver()
            stopService()  //结束服务
            mDefaultDialog?.dismissAll()
            if (mCallback != null) {
                mCallback.onLoadFailed()
                mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
            } else {
                Toast.makeText(mApplicationContext, "sorry, 更新失败了~", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onLoadPaused() {
            mCallback?.onLoadPaused()
        }

        override fun onLoadPending() {
            mCallback?.onLoadPending()
        }
    }

    /**
     * 结束下载任务
     */
    fun endDownloadTask() {
        if (mOnProgressListener != null) {
            mOnProgressListener?.onLoadFailed()
        }
    }

    private inner class DefaultDialogListener : DefaultDialog.DialogListener {
        private var mCurrentState: Int = 0

        /**
         * 改变状态。
         *
         * @param currentState 要改变新状态。
         * @return 返回 DefaultDialogListener 本身。
         */
        internal fun changeState(currentState: Int): DefaultDialogListener {
            mCurrentState = currentState
            return this
        }

        override fun onDialogDismiss(isSure: Boolean) {
            when (mCurrentState) {
                STATE_CHECK_UPDATE//版本检测
                -> respondCheckHandlerResult(isSure)
                STATE_NETWORK_UNUSABLE//网络不可用
                -> if (mCallback != null) {
                    if (isBindService) {
                        mCallback.onLoadCancelled()
                        mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
                    } else {
                        mCallback.onCheckCancelled()
                        mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
                    }
                }
                STATE_WIFI_UNUSABLE//wifi不可用
                -> if (isSure) {
                    startDownload()
                } else {
                    if (mCallback != null) {
                        mCallback.onLoadCancelled()
                        mCallback.onCompleted(true, UpdateHelper.getCurrentVersionName(mApplicationContext))
                    }
                }
                STATE_DOWNLOAD//下载状态
                -> mIsProgressDialogHidden = true
            }
        }

        /**
         * 表示当前的状态是检查更新。
         */
        val STATE_CHECK_UPDATE = 2001
        /**
         * 表示当前的状态是无网络。
         */
        val STATE_NETWORK_UNUSABLE = 2002
        /**
         * 表示当前的状态是无WiFi。
         */
        val STATE_WIFI_UNUSABLE = 2003
        /**
         * 表示当前的状态是下载中。
         */
        val STATE_DOWNLOAD = 2004
    }

    companion object {
        private var sLocalVersionName = "1.0.0"
        private var sLocalVersionCode = 20001L

        /**
         * 判断当前版本是否是强制更新。
         *
         * @return 如果是返回true，否则返回false。
         */
        fun isForceUpdate(updateInfo: UpdateInfo, context: Context): Boolean {
            if (!updateInfo.isForceUpdate!!) {
                return false
            } else {
                val codes = updateInfo.forceUpdateVersionCodes
                if (codes == null || codes.isEmpty()) {
                    return true
                } else {
                    for (code in codes) {
                        if (getLocalVersionCode(context) == code) {
                            return true
                        }
                    }
                    return false
                }
            }
        }

        private fun getLocalVersionCode(context: Context): Long {
            if (sLocalVersionCode == 20001L) {
                sLocalVersionCode = UpdateHelper.getCurrentVersionCode(context)
            }
            return sLocalVersionCode
        }

        private fun getLocalVersionName(context: Context): String {
            sLocalVersionName = UpdateHelper.getCurrentVersionName(context)
            return sLocalVersionName
        }
    }

}
/**
 * 检查更新并自动安装。
 *
 * @param updateInfo 更新信息对象。
 */
/**
 * 开始下载。
 *
 * @param updateInfo 更新信息对象。
 */
