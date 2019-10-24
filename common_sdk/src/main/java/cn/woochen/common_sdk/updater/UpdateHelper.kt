package cn.woochen.common_sdk.updater

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.File


/**
 * 描述 工具集合类。
 * 创建人 kelin
 * 创建时间 2016/10/14  下午12:26
 */

class UpdateHelper private constructor() {

    init {
        throw InstantiationError("Utility class don't need to instantiate！")
    }

    companion object {

        private val CONFIG_NAME = "apkUpdater_config"
        /**
         * apk 文件存储路径
         */
        private val SP_KEY_DOWNLOAD_APK_PATH = "apkUpdater.apkPath"
        /**
         * 上一次下载的APK的版本号。
         */
        private val SP_KEY_DOWNLOAD_APK_VERSION_CODE = "apkUpdater.apkVersionCode"

        private val SP_KEY_DOWNLOAD_APK_VERSION_NAME = "apkUpdater.apkVersionName"

        /**
         * 获取当前的版本号。
         * @param context 需要一个上下文。
         * @return 返回当前的版本号。
         */
        fun getCurrentVersionCode(context: Context): Long {
            val packageManager = context.packageManager
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return packageInfo?.longVersionCode ?: 0
        }

        /**
         * 获取当前的版本名称。
         * @param context 需要一个上下文。
         * @return 返回当前的版本名称。
         */
        fun getCurrentVersionName(context: Context): String {
            val packageManager = context.packageManager
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return if (packageInfo != null) packageInfo.versionName else "未知版本"
        }


        /**
         * 安装APK
         * @param context [Activity] 对象。
         * @param apkFile 安装包的路径
         */
        fun installApk(context: Context, apkFile: File?): Boolean {
            if (apkFile == null || !apkFile.exists()) {
                return false
            }
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory("android.intent.category.DEFAULT")
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 给目标应用一个临时授权
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val uri =
                    FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", apkFile)
                intent.setDataAndType(uri, context.contentResolver.getType(uri))
                val resolveLists =
                    context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                // 然后全部授权
                for (resolveInfo in resolveLists) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), getIntentType(apkFile))
            }
            try {

                context.startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context.applicationContext, "安装出现未知问题", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        }

        private fun getIntentType(file: File): String? {
            val suffix = file.name
            val name = suffix.substring(suffix.lastIndexOf(".") + 1, suffix.length).toLowerCase()
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(name)
        }

        /**
         * 删除上次更新存储在本地的apk
         */
         fun removeOldApk(context: Context) {
            //获取老ＡＰＫ的存储路径
            val apkFile = File(getApkPathFromSp(context))

            if (apkFile.exists() && apkFile.isFile) {
                if (apkFile.delete()) {
                    getEdit(context).remove(SP_KEY_DOWNLOAD_APK_PATH)
                    getEdit(context).remove(SP_KEY_DOWNLOAD_APK_VERSION_CODE)
                    getEdit(context).remove(SP_KEY_DOWNLOAD_APK_VERSION_NAME)
                }
            }
        }

         fun putApkPath2Sp(context: Context, value: String) {
            getEdit(context).putString(SP_KEY_DOWNLOAD_APK_PATH, value).commit()
        }

         fun getApkPathFromSp(context: Context): String {
            return context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE)
                .getString(SP_KEY_DOWNLOAD_APK_PATH, "")!!
        }

         fun putApkVersionCode2Sp(context: Context, value: Int) {
            getEdit(context).putInt(SP_KEY_DOWNLOAD_APK_VERSION_CODE, value).commit()
        }

         fun putApkVersionName2Sp(context: Context, value: String) {
            getEdit(context).putString(SP_KEY_DOWNLOAD_APK_VERSION_NAME, value).commit()
        }

         fun getApkVersionCodeFromSp(context: Context): Int {
            return context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE)
                .getInt(SP_KEY_DOWNLOAD_APK_VERSION_CODE, -1)
        }

         fun getApkVersionNameFromSp(context: Context): String {
            return context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE)
                .getString(SP_KEY_DOWNLOAD_APK_VERSION_NAME,"")
        }

        /**
         * 这个方法是返回一个sharedPreferences的Edit编辑器
         *
         * @param context 上下文
         * @return 返回一个Edit编辑器。
         */
        private fun getEdit(context: Context): SharedPreferences.Editor {
            val sp = context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE)
            return sp.edit()
        }
    }
}
