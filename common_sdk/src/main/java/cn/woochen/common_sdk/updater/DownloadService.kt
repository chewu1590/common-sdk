package cn.woochen.common_sdk.updater

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Service
import android.content.*
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import cn.woochen.common_sdk.updater.callback.DownloadProgressCallback
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 描述 下载APK的服务。
 * 创建人 kelin
 * 创建时间 2017/3/15  上午10:18
 * 版本 v 1.0.0
 */

class DownloadService : Service() {

    private var downloadManager: DownloadManager? = null
    private var downloadObserver: DownloadChangeObserver? = null
    private var downLoadBroadcast: BroadcastReceiver? = null
    private var scheduledExecutorService: ScheduledExecutorService? = null

    //下载任务ID
    private var downloadId: Long = 0
    private var onProgressListener: DownloadProgressCallback? = null

    private  var downLoadHandler: Handler? = null

    private var progressRunnable: Runnable? = null
    private var mNotifyTitle: String? = null
    private var mIsForceUpdate: Boolean = false
    private var mNotifyDescription: String? = null
    private var mApkName: String? = null
    private var mLastFraction = 20000
    //    private int mLastFraction = 0xFFFF_FFFF;
    /**
     * 服务被解绑的监听。
     */
    private var serviceUnBindListener: ServiceUnBindListener? = null
    private var mIsLoadFailed: Boolean = false
    private var mCursor: Cursor? = null
    private var mLastStatus: Int = 0
    private var mIsStarted: Boolean = false

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     */
    private//已经下载文件大小
    //下载文件的总大小
    //下载状态
    val bytesAndStatus: IntArray
        get() {
            val bytesAndStatus = intArrayOf(-1, -1, 0)
            if (mCursor == null) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                mCursor = downloadManager!!.query(query)
            } else {
                mCursor!!.requery()
            }
            if (mCursor != null && mCursor!!.moveToFirst()) {
                bytesAndStatus[0] =
                    mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                bytesAndStatus[1] =
                    mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                bytesAndStatus[2] = mCursor!!.getInt(mCursor!!.getColumnIndex(DownloadManager.COLUMN_STATUS))
            }
            return bytesAndStatus
        }

    private//被除数可以为0，除数必须大于0
    val handler: Handler
        @SuppressLint("HandlerLeak")
        get() = if (downLoadHandler != null) downLoadHandler!! else object : Handler() {
            override fun handleMessage(msg: Message) {
                if (onProgressListener != null) {
                    when (msg.what) {
                        WHAT_PROGRESS -> {
                            val obj = msg.obj as Int
                            if (obj == DownloadManager.STATUS_RUNNING || mLastStatus != obj) {
                                mLastStatus = obj
                                when (obj) {
                                    DownloadManager.STATUS_FAILED -> {
                                        onProgressListener!!.onLoadFailed()
                                        mIsLoadFailed = true
                                    }
                                    DownloadManager.STATUS_PAUSED -> onProgressListener!!.onLoadPaused()
                                    DownloadManager.STATUS_PENDING -> onProgressListener!!.onLoadPending()
                                    DownloadManager.STATUS_RUNNING -> {
                                        if (mLastFraction == 20000 && !mIsStarted) {
                                            mIsStarted = true
                                            onProgressListener!!.onStartDownLoad()
                                        }
                                        if (msg.arg1 >= 0 && msg.arg2 > 0) {
                                            var fraction = ((msg.arg1 + 0f) / msg.arg2 * 100).toInt()
                                            if (fraction == 0) fraction = 1
                                            if (mLastFraction != fraction) {
                                                mLastFraction = fraction
                                                onProgressListener!!.onProgress(
                                                    msg.arg1.toLong(),
                                                    msg.arg2.toLong(),
                                                    mLastFraction
                                                )
                                            }
                                        }
                                    }
                                    DownloadManager.STATUS_SUCCESSFUL -> if (msg.arg1 >= 0 && msg.arg2 > 0) {
                                        var fraction = ((msg.arg1 + 0f) / msg.arg2 * 100).toInt()
                                        if (fraction == 0) fraction = 1
                                        if (mLastFraction != fraction) {
                                            mLastFraction = fraction
                                            onProgressListener!!.onProgress(
                                                msg.arg1.toLong(),
                                                msg.arg2.toLong(),
                                                mLastFraction
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        WHAT_COMPLETED -> if (!mIsLoadFailed) {
                            val apkFile = msg.obj as File
                            onProgressListener!!.onLoadSuccess(apkFile, false)
                        }
                    }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        if (downLoadHandler == null) {
            downLoadHandler = handler
        }
        progressRunnable = Runnable { updateProgress() }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        val downloadUrl = intent.getStringExtra(KEY_DOWNLOAD_URL)
        mNotifyTitle = intent.getStringExtra(KEY_NOTIFY_TITLE)
        mIsForceUpdate = intent.getBooleanExtra(KEY_IS_FORCE_UPDATE, false)
        mNotifyDescription = intent.getStringExtra(KEY_NOTIFY_DESCRIPTION)
        mApkName = intent.getStringExtra(KEY_APK_NAME)
        downloadApk(downloadUrl)
        return DownloadBinder()
    }

    /**
     * 下载最新APK
     */
    private fun downloadApk(url: String) {
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadObserver = DownloadChangeObserver()

        registerContentObserver()

        val request = DownloadManager.Request(Uri.parse(url))
        val visibility =
            if (mIsForceUpdate) DownloadManager.Request.VISIBILITY_HIDDEN else DownloadManager.Request.VISIBILITY_VISIBLE
        request.setTitle(mNotifyTitle).setDescription(mNotifyDescription).setNotificationVisibility(visibility)
            .setDestinationInExternalFilesDir(applicationContext, Environment.DIRECTORY_DOWNLOADS, mApkName)
        /*将下载请求放入队列， return下载任务的ID*/
        downloadId = downloadManager!!.enqueue(request)
        registerBroadcast()
    }

    /**
     * 注册广播
     */
    private fun registerBroadcast() {
        /*注册service 广播 1.任务完成时 2.进行中的任务被点击*/
        registerReceiver(DownLoadBroadcast(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     * 注销广播
     */
    private fun unregisterBroadcast() {
        if (downLoadBroadcast != null) {
            unregisterReceiver(downLoadBroadcast)
            downLoadBroadcast = null
        }
    }

    /**
     * 注册ContentObserver
     */
    private fun registerContentObserver() {
        if (downloadObserver != null) {
            contentResolver.registerContentObserver(
                Uri.parse("content://downloads/my_downloads"),
                false,
                downloadObserver!!
            )
        }
    }

    /**
     * 注销ContentObserver
     */
    private fun unregisterContentObserver() {
        if (downloadObserver != null) {
            contentResolver.unregisterContentObserver(downloadObserver!!)
        }
    }

    /**
     * 关闭定时器，线程等操作
     */
    private fun close() {
        if (scheduledExecutorService != null && !scheduledExecutorService!!.isShutdown) {
            scheduledExecutorService!!.shutdown()
        }

        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }

        if (downLoadHandler != null) {
            downLoadHandler!!.removeCallbacksAndMessages(null)
            downLoadHandler = null
        }
    }

    /**
     * 发送Handler消息更新进度和状态
     */
    private fun updateProgress() {
        val bytesAndStatus = bytesAndStatus
        downLoadHandler!!.sendMessage(
            downLoadHandler!!.obtainMessage(
                WHAT_PROGRESS,
                bytesAndStatus[0],
                bytesAndStatus[1],
                bytesAndStatus[2]
            )
        )
    }

    /**
     * 接受下载完成广播
     */
    private inner class DownLoadBroadcast : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            when (intent.action) {
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> if (downloadId == downId && downId != -1L && downloadManager != null) {
                    val apkFile = getRealFile(downloadManager!!.getUriForDownloadedFile(downloadId))

                    if (apkFile != null && apkFile.exists()) {
                        val realPath = apkFile.absolutePath
                        UpdateHelper.putApkPath2Sp(applicationContext, realPath)
                    }
                    updateProgress()
                    downLoadHandler!!.sendMessage(downLoadHandler!!.obtainMessage(WHAT_COMPLETED, apkFile))
                }
            }
        }
    }

    fun getRealFile(uri: Uri?): File? {
        if (null == uri) return null
        val scheme = uri.scheme
        var path: String? = null
        if (scheme == null || ContentResolver.SCHEME_FILE == scheme) {
            path = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = applicationContext.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        path = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return if (null == path) null else File(path)
    }

    /**
     * 监听下载进度
     */
    private inner class DownloadChangeObserver internal constructor() : ContentObserver(downLoadHandler) {

        init {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        }

        /**
         * 当所监听的Uri发生改变时，就会回调此方法
         *
         * @param selfChange 此值意义不大, 一般情况下该回调值false
         */
        override fun onChange(selfChange: Boolean) {
            synchronized(this.javaClass) {
                scheduledExecutorService!!.scheduleAtFixedRate(progressRunnable, 0, 100, TimeUnit.MILLISECONDS)
            }
        }
    }

    internal inner class DownloadBinder : Binder() {
        /**
         * 返回当前服务的实例
         *
         * @return 返回 [DownloadService] 对象。
         */
        val service: DownloadService
            get() = this@DownloadService

    }

    /**
     * 设置进度更新监听。
     *
     * @param onProgressListener [DownloadProgressCallback] 的实现类对象。
     */
     fun setOnProgressListener(onProgressListener: DownloadProgressCallback) {
        this.onProgressListener = onProgressListener
    }

    /**
     * 设置进度更新监听。
     *
     * @param serviceUnBindListener [ServiceUnBindListener] 的实现类对象。
     */
     fun setServiceUnBindListener(serviceUnBindListener: ServiceUnBindListener) {
        this.serviceUnBindListener = serviceUnBindListener
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (serviceUnBindListener != null) {
            serviceUnBindListener!!.onUnBind()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        close()
        unregisterBroadcast()
        unregisterContentObserver()
        if (downloadManager != null) {
            downloadManager!!.remove(downloadId)
        }
    }

    interface ServiceUnBindListener {

        /**
         * 当服务被解绑的时候回调。
         */
        fun onUnBind()
    }

    companion object {

        /**
         * 表示当前的消息类型为更新进度。
         */
        val WHAT_PROGRESS = 10000
        //    public static final int WHAT_PROGRESS = 0x0000_0101;
        /**
         * 表示当前的消息类型为下载完成。
         */
        private val WHAT_COMPLETED = 10001
        //    private static final int WHAT_COMPLETED = 0X0000_0102;
        /**
         * 用来获取下载地址的键。
         */
        val KEY_DOWNLOAD_URL = "download_url"
        /**
         * 用来获取通知栏标题的键。
         */
        val KEY_NOTIFY_TITLE = "key_notify_title"
        /**
         * 用来获取通知栏描述的键。
         */
        val KEY_NOTIFY_DESCRIPTION = "key_notify_description"
        /**
         * 用来获取是否强制更新的键。
         */
        val KEY_IS_FORCE_UPDATE = "key_is_force_update"
        /**
         * 用来获取APK名称的键。
         */
        val KEY_APK_NAME = "key_apk_name"
    }
}
