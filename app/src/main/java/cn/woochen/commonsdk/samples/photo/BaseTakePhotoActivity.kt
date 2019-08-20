package cn.woochen.commonsdk.samples.photo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.woochen.common_sdk.takephoto.core.TakePhoto
import cn.woochen.common_sdk.takephoto.core.TakePhotoImpl
import cn.woochen.common_sdk.takephoto.model.InvokeParam
import cn.woochen.common_sdk.takephoto.model.TContextWrap
import cn.woochen.common_sdk.takephoto.model.TResult
import cn.woochen.common_sdk.takephoto.permission.InvokeListener
import cn.woochen.common_sdk.takephoto.permission.PermissionManager
import cn.woochen.common_sdk.takephoto.permission.TakePhotoInvocationHandler
import cn.woochen.commonsdk.R

/**
 *继承这个类来让Activity获取拍照的能力
 *@author woochen
 *@time 2019/4/11 15:39
 */
abstract class  BaseTakePhotoActivity :AppCompatActivity() , TakePhoto.TakeResultListener, InvokeListener {


    private var invokeParam: InvokeParam? = null
    private var takePhoto: TakePhoto? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        getTakePhoto().onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        getTakePhoto().onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this)
    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    fun getTakePhoto(): TakePhoto {
        if (takePhoto == null) {
            takePhoto = TakePhotoInvocationHandler.of(this).bind(TakePhotoImpl(this, this)) as TakePhoto
        }
        return takePhoto as TakePhoto
    }

    override fun takeSuccess(result: TResult) {
        println( "takeSuccess：" + result.image.compressPath)
    }

    override fun takeFail(result: TResult?, msg: String) {
        println("takeFail:$msg")
    }

    override fun takeCancel() {
        println( resources.getString(R.string.msg_operation_canceled))
    }

    override fun invoke(invokeParam: InvokeParam): PermissionManager.TPermissionType {
        val type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.method)
        if (PermissionManager.TPermissionType.WAIT == type) {
            this.invokeParam = invokeParam
        }
        return type
    }

}