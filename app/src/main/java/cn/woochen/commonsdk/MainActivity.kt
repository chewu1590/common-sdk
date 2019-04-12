package cn.woochen.commonsdk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.woochen.commonsdk.samples.TakePhotoActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v) {
            btn_take_photo -> {
               start(TakePhotoActivity::class.java)
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
    }

    private fun initListener() {
        btn_take_photo.setOnClickListener(this)
    }

    private fun start(clazz: Class<*>){
        val intent = Intent(this,clazz)
        startActivity(intent)
    }

}
