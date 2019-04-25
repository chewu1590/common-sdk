package cn.woochen.commonsdk.samples.photo

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.woochen.commonsdk.R
import cn.woochen.commonsdk.samples.adapter.PreviewImageAdapter
import kotlinx.android.synthetic.main.fragment_preview_dialog.*


/**
 *图片预览
 *@author woochen
 *@time 2019/4/12 16:36
 */
class PreviewDialogFragment : DialogFragment() {
    private var mCurrentIndex: Int = 0
    private lateinit var mImagePaths: ArrayList<String>
    private val mPreviewImageAdapter by lazy {
        PreviewImageAdapter(context!!, mImagePaths)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_dialog_fragment)
        arguments?.let {
            mCurrentIndex = it.getInt(CURRENT_INDEX,0)
            mImagePaths = it.getStringArrayList(IMAGE_LIST)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_dialog, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    private fun initData() {
        tv_page.text = "${mCurrentIndex + 1}/${mImagePaths.size}"
        vp_preview.adapter = mPreviewImageAdapter
        vp_preview.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mCurrentIndex = position
                tv_page.text = "${mCurrentIndex + 1}/${mImagePaths.size}"
            }
        })
        mPreviewImageAdapter.onItemClickListener = object: PreviewImageAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                dismiss()
            }
        }
        vp_preview.currentItem = mCurrentIndex
    }

    /**
     * 显示弹窗
     */
    fun show(context: Context){
        val tag = "preview_dialog"
        var manager: FragmentManager? = null
        if (context is Fragment){
            manager = context.fragmentManager
        }
        if (context is AppCompatActivity){
            manager = context.supportFragmentManager
        }
        show(manager,tag)
    }


    companion object {
        private const val CURRENT_INDEX = "CURRENT_INDEX"
        private const val IMAGE_LIST = "IMAGE_LIST"
        @JvmStatic
        fun newInstance(currentIndex: Int, imageList: ArrayList<String>) =
            PreviewDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(CURRENT_INDEX, currentIndex)
                    putStringArrayList(IMAGE_LIST, imageList)
                }
            }
    }
}
