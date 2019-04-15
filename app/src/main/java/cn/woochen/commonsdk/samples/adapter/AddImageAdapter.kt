package cn.woochen.commonsdk.samples.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.woochen.commonsdk.R
import cn.woochen.commonsdk.samples.photo.PreviewDialogFragment
import com.bumptech.glide.Glide

/**
 *添加图片
 *@author woochen
 *@time 2019/4/12 9:47
 */
class AddImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private var mDatas: ArrayList<String>
    private var mContext: Context
    private var layoutInflater: LayoutInflater
    var onItemClickListener: OnItemClickListener? = null


    constructor(context: Context, datas: ArrayList<String>) : super() {
        mContext = context
        mDatas = datas
        layoutInflater = LayoutInflater.from(mContext)
    }


    override fun getItemViewType(position: Int): Int {
        var type: Int
        if (position == 0) {
            type = ImageType.IMAGE_ADD.value
        }else{
            type = ImageType.IMAGE_SHOW.value
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutResId: Int
        if (viewType == ImageType.IMAGE_ADD.value) {
            layoutResId = R.layout.item_add
        } else {
            layoutResId = R.layout.item_show
        }
        val inflate = layoutInflater.inflate(layoutResId, parent, false)
        return ViewHolder(inflate)
    }

    override fun getItemCount(): Int = mDatas.size + 1

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            viewHolder.itemView.setOnClickListener {
                onItemClickListener?.add()
            }
        } else {
            var ivPic = viewHolder.itemView.findViewById<ImageView>(R.id.iv_pic_show)
            var ivDel = viewHolder.itemView.findViewById<ImageView>(R.id.iv_del)
            Glide.with(mContext).load(mDatas[position - 1]).into(ivPic)
            ivPic.setOnClickListener {
//                PreviewPhotoActivity.start(mContext,mDatas,position - 1)
                PreviewDialogFragment.newInstance(position-1,mDatas).show(mContext)
            }
            ivDel.setOnClickListener {
                mDatas.removeAt(position - 1)
                notifyDataSetChanged()
            }
        }
    }

    enum class ImageType(var value: Int) {
        IMAGE_ADD(1), IMAGE_SHOW(2)
    }

    class ViewHolder : RecyclerView.ViewHolder {
        constructor(itemView: View) : super(itemView)
    }

    interface OnItemClickListener {
        fun add()
    }
}