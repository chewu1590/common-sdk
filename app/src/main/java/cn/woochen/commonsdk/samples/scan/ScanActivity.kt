package cn.woochen.commonsdk.samples.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import cn.woochen.commonsdk.R
import com.skateboard.zxinglib.CaptureActivity
import com.umeng.commonsdk.stateless.UMSLEnvelopeBuild.mContext
import kotlinx.android.synthetic.main.activity_scan.*

/**
 *二维码扫描示例类
 *@author woochen
 *@time 2019/4/22 18:33
 */
class ScanActivity : AppCompatActivity(), View.OnClickListener {
    private val SCAN_REQUEST_CODE = 0x0010
    private var allpermissions = arrayOf(Manifest.permission.CAMERA)


    override fun onClick(v: View?) {
        when (v) {
            btn_scan -> {
                applyPermission()
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        initView()
    }

    private fun initView() {
        btn_scan.setOnClickListener(this)
    }




    fun applyPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            var needapply = false
            for (i in 0 until allpermissions.size) {
                val chechpermission = ContextCompat.checkSelfPermission(applicationContext, allpermissions[i])
                if (chechpermission != PackageManager.PERMISSION_GRANTED) {
                    needapply = true
                }
            }
            if (needapply) {
                ActivityCompat.requestPermissions(this@ScanActivity, allpermissions, 1)
            }else{
                toScan()
            }
        }else{
            toScan()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isAllGranted(permissions,grantResults)){
            toScan()
        }
    }

    private fun toScan() {
        val intent = Intent(this@ScanActivity, CaptureActivity::class.java)
        startActivityForResult(intent, SCAN_REQUEST_CODE)
    }

    private fun isAllGranted(permissions: Array<String>,grantResults: IntArray) :Boolean{
        for (index in grantResults.indices) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@ScanActivity, permissions[index] + "未授权", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }
    
}
