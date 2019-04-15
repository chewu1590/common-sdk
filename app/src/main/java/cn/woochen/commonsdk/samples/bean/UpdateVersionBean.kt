package cn.woochen.commonsdk.samples.bean

import cn.woochen.common_sdk.updater.UpdateInfo

class UpdateVersionBean : UpdateInfo, DefaultValue {

    override var versionCode: Int? = null
        get() = defaultValue(field, 1)
    override var versionName: String? = null
        get() = defaultValue(field, "1.0")
    override var downLoadsUrl: String? = null
        get() = defaultValue(field, "https://image.imyxg.com/client1.9.1.apk")
    override var isForceUpdate: Boolean? = null
        get() = defaultValue(field, false)
    override var forceUpdateVersionCodes: IntArray? = null
        get() = defaultValue(field, IntArray(1))
    override var apkName: String? = null
        get() = defaultValue(field, "common-sdk")
    override var updateMessage: CharSequence? = null
        get() = defaultValue(field, "修复已知bug")
    override var apkSize: Double? = null
        get() = defaultValue(field, 1.1)
}

