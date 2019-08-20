package cn.woochen.commonsdk.samples

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import cn.woochen.common_sdk.takephoto.model.TResult
import cn.woochen.commonsdk.samples.adapter.AddImageAdapter
import cn.woochen.commonsdk.samples.photo.BaseTakePhotoActivity
import cn.woochen.commonsdk.samples.photo.TakePhotoDialog
import cn.woochen.commonsdk.samples.util.FileUtil
import kotlinx.android.synthetic.main.activity_take_photo.*


/**
 *拍照演示类
 *@author woochen
 *@time 2019/4/11 15:44
 */
class TakePhotoActivity : BaseTakePhotoActivity() {

    private val picPaths = arrayListOf<String>()
    private val mAddImageAdapter: AddImageAdapter by lazy {
        AddImageAdapter(this@TakePhotoActivity, picPaths)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cn.woochen.commonsdk.R.layout.activity_take_photo)
        initView()
    }

    private fun initView() {
        rv_add.layoutManager = GridLayoutManager(this, 3)
        rv_add.adapter = mAddImageAdapter
        mAddImageAdapter.onItemClickListener = object : AddImageAdapter.OnItemClickListener {
            override fun add() {
                val takePhotoDialog = TakePhotoDialog.newInsatance()
                takePhotoDialog.setTakePhoto(getTakePhoto(), false)
                takePhotoDialog.show(supportFragmentManager, "takePhoto")
            }
        }
    }

    override fun takeSuccess(result: TResult) {
       runOnUiThread {
           for (image in result.images) {
               Log.e("image originalPath->", image.originalPath + " size ->" + FileUtil.getFileSize(image.originalPath))
               Log.e("image compressPath->", image.compressPath + " size ->" + FileUtil.getFileSize(image.compressPath))
               picPaths.add(image.compressPath)
           }
           mAddImageAdapter.notifyDataSetChanged()
       }
    }

}
