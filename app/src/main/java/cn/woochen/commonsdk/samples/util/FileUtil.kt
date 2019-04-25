package cn.woochen.commonsdk.samples.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat

object FileUtil {

    /**
     * 获取指定文件大小
     */
    @Throws(Exception::class)
    fun getFileSize(filePath: String):String {
        val file = File(filePath)
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream?
            fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            Log.e("获取文件大小", "文件不存在!")
        }
        return toFileSize(size)
    }


    /**
     * 获取指定文件大小
     */
    @Throws(Exception::class)
    fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream?
            fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            Log.e("获取文件大小", "文件不存在!")
        }
        return size
    }


    /**
     * 转换文件大小
     */
    fun toFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        var fileSizeString: String
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        if (fileS < 1024) {
            fileSizeString = df.format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            fileSizeString = df.format(fileS.toDouble() / 1024) + "KB"
        } else if (fileS < 1073741824) {
            fileSizeString = df.format(fileS.toDouble() / 1048576) + "MB"
        } else {
            fileSizeString = df.format(fileS.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    }




}