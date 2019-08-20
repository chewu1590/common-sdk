package cn.woochen.common_sdk.takephoto.helper

import android.net.Uri
import android.os.Environment
import cn.woochen.common_sdk.takephoto.compress.CompressConfig
import cn.woochen.common_sdk.takephoto.core.TakePhoto
import cn.woochen.common_sdk.takephoto.model.CropOptions
import cn.woochen.common_sdk.takephoto.model.LubanOptions
import cn.woochen.common_sdk.takephoto.model.TakePhotoOptions
import java.io.File

/**
 *拍照辅助类
 *@author woochen
 *@time 2019/4/10 18:48
 */
class TakePhotoHelper(private val mTakePhoto: TakePhoto) {

    private val imageUri: Uri
    private var isCrop: Boolean = false
    private var mCropOption: CropOptions? = null

    init {
        val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        imageUri = Uri.fromFile(file)
    }

    /**
     * 剪裁配置
     * @param isCrop 是否剪裁
     */
    fun setCorpOption(isCrop: Boolean): TakePhotoHelper {
        this.isCrop = isCrop
        if (!isCrop) {
            return this
        }
        val width = 800
        val height = 800
        //是否剪裁
        //剪裁工具
        //        尺寸/比例
        val builder = CropOptions.Builder()
        builder.setAspectX(width).setAspectY(height)//宽 / 高
        builder.setOutputX(width).setOutputY(height)//宽 * 高
        builder.setWithOwnCrop(true)//是否使用自带的剪裁工具
        this.mCropOption = builder.create()
        return this
    }

    /**
     * 压缩配置
     * @param isOwn          是否使用自带的压缩工具
     * @param isSaveOriginal 是否保存原始图片
     * @param isShowProgress 是否显示压缩进度条
     */
    fun setCompressOption(isOwn: Boolean, isSaveOriginal: Boolean, isShowProgress: Boolean): TakePhotoHelper {
        val config: CompressConfig
        val maxSize = 102400//文件大小
        val width = 800//宽度
        val height = 800//高度
        if (isOwn) {
            config = CompressConfig.Builder().setMaxSize(maxSize)
                .setMaxPixel(if (width >= height) width else height)
                .enableReserveRaw(isSaveOriginal)//是否保存原始图片
                .create()
        } else {
            val option = LubanOptions.Builder().setMaxHeight(height).setMaxWidth(width).setMaxSize(maxSize).create()
            config = CompressConfig.ofLuban(option)
            config.enableReserveRaw(isSaveOriginal)
        }
        mTakePhoto.onEnableCompress(config, isShowProgress)
        return this
    }

    /**
     * 相册配置
     * @param isOwn        是否使用自带的相册
     * @param correctAngle 纠正拍照角度
     */
    fun setPicOption(isOwn: Boolean, correctAngle: Boolean): TakePhotoHelper {
        val builder = TakePhotoOptions.Builder()
        builder.setWithOwnGallery(isOwn)
        builder.setCorrectImage(true)
        mTakePhoto.setTakePhotoOptions(builder.create())
        return this
    }


    /**
     * 拍照
     */
    fun takePhoto() {
        if (isCrop) {
            mTakePhoto.onPickFromCaptureWithCrop(imageUri, mCropOption)
        } else {
            mTakePhoto.onPickFromCapture(imageUri)
        }
    }


    /**
     * 从相册选择
     */
    fun pickFormGallery(limitCount: Int) {
        var limit = 1
        if (limitCount > 1) limit = limitCount
        if (limit > 1) {
            if (isCrop) {
                mTakePhoto.onPickMultipleWithCrop(limit, mCropOption)//图片大于一张剪裁
            } else {
                mTakePhoto.onPickMultiple(limit)//图片大于一张不剪裁
            }
            return
        }
        //一张从相册中选择
        if (isCrop) {
            mTakePhoto.onPickFromGalleryWithCrop(imageUri, mCropOption)
        } else {
            mTakePhoto.onPickFromGallery()
        }

    }
}
