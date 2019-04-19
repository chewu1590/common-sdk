package cn.woochen.share

import android.app.Activity

interface IShareChannel {

    /**
     * 分享链接
     */
//    fun shareWithLink()

    /**
     * 分享文本
     */
    fun shareWithText(activity: Activity)

    /**
     * 分享图片
     */
//    fun shareWithPic()

    /**
     * 设置分享类型
     */
    fun setShareTypes(shareTypes: Array<out ShareType>)

}