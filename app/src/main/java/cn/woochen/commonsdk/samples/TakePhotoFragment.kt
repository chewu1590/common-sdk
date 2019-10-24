package cn.woochen.commonsdk.samples

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.woochen.common_sdk.takephoto.model.TResult
import cn.woochen.commonsdk.R
import cn.woochen.commonsdk.samples.adapter.AddImageAdapter
import cn.woochen.commonsdk.samples.photo.BaseTakePhotoFragment
import cn.woochen.commonsdk.samples.photo.TakePhotoDialog
import cn.woochen.commonsdk.samples.util.FileUtil
import kotlinx.android.synthetic.main.fragment_take_photo.*

class TakePhotoFragment : BaseTakePhotoFragment() {

    private val picPaths = arrayListOf<String>()
    private val mAddImageAdapter: AddImageAdapter by lazy {
        AddImageAdapter(context!!, picPaths)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate = inflater.inflate(R.layout.fragment_take_photo, container, false)
        return inflate
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }


    private fun initView() {
        rv_add.layoutManager = GridLayoutManager(context!!, 3)
        rv_add.adapter = mAddImageAdapter
        mAddImageAdapter.onItemClickListener = object : AddImageAdapter.OnItemClickListener {
            override fun add() {
                val takePhotoDialog = TakePhotoDialog.newInsatance()
                takePhotoDialog.setTakePhoto(getTakePhoto()!!, false)
                if (fragmentManager != null) takePhotoDialog.show(fragmentManager!!, "takePhoto")
            }
        }
    }

    override fun takeSuccess(result: TResult) {
        (context as Activity).runOnUiThread {
            for (image in result.images) {
                Log.e("image originalPath->", image.originalPath + " size ->" + FileUtil.getFileSize(image.originalPath))
                Log.e("image compressPath->", image.compressPath + " size ->" + FileUtil.getFileSize(image.compressPath))
                picPaths.add(image.compressPath)
            }
            mAddImageAdapter.notifyDataSetChanged()
        }

    }
}