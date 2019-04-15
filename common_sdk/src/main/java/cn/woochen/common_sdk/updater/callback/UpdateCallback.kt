package cn.woochen.common_sdk.updater.callback

import java.io.File

/**
 * 描述 下载文件的回调接口。
 * 创建人 kelin
 * 创建时间 2016/10/11  上午11:10
 * 包名 com.chengshi.downloader.callbacks
 */

open class UpdateCallback : DownloadProgressCallback {

    /**
     * 开始下载，在开始执行下载的时候调用。
     */
    override fun onStartDownLoad() {
        println(TAG + "onStartDownLoad: ")
    }

    /**
     * 下载完成。
     *
     * @param apkFile 已经下载好的APK文件对象。
     * @param isCache 是否是缓存，如果改参数为true说明本次并没有真正的执行下载任务，因为上一次用户下载完毕后并没有进行
     */
    override fun onLoadSuccess(apkFile: File, isCache: Boolean) {
        println(TAG + "onLoadSuccess: ")
    }

    /**
     * 当下载失败的时候调用。
     */
    override fun onLoadFailed() {
        println(TAG + "onLoadFailed: ")
    }

    /**
     * 下载暂停。
     */
    override fun onLoadPaused() {
        println(TAG + "onLoadPaused: ")
    }

    /**
     * 等待下载。
     */
    override fun onLoadPending() {
        println(TAG + "onLoadPending: ")
    }

    /**
     * 检查更新被取消。如果当前设备无网络可用则会执行该方法。
     */
    fun onCheckCancelled() {
        println(TAG + "onCheckCancelled: ")
    }

    /**
     * 当下载被取消后调用。即表明用户不想进行本次更新，强制更新一般情况下是不能取消的，除非你设置了需要检查WIFI而WIFI又没有链接。
     */
    fun onLoadCancelled() {
        println(TAG + "onLoadCancelled: ")
    }

    /**
     * 如果在安装过程中发生了意外导致安装失败会执行此方法。
     */
    fun onInstallFailed() {
        println(TAG + "onInstallFailed: ")
    }

    /**
     * 当任务完毕后被调用。无论任务成功还是失败，也无论是否需要更新。如果在检查更新阶段发现没有新的版本则会直接执行
     * 该方法，如果检查更新失败也会执行该方法，如果检测到了新的版本的话，那么这个方法就不会再检查更新阶段调用，一直
     * 等到下载完成或下载失败之后才会被执行。
     *
     * @param haveNewVersion 是否有新的版本。
     * `true`表示有新的版本,
     * `false`则表示没有新的版本。
     * @param curVersionName 当前app的版本名称。
     */
    open fun onCompleted(haveNewVersion: Boolean, curVersionName: String) {
        println(TAG + "onCompleted: haveNewVersion->" + haveNewVersion + " curVersionName->" + curVersionName)
    }

    /**
     * 下载进度更新的时候调用。
     *
     * @param total      文件总大小(字节)。
     * @param current    当前的进度(字节)。
     * @param percentage 当前下载进度的百分比。
     */
    override fun onProgress(total: Long, current: Long, percentage: Int) {
        println(TAG + "onProgress: ")
    }

    companion object {
        private val TAG = "update"
    }
}
