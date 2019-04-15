import cn.woochen.commonsdk.samples.bean.DefaultValue

class UserBean : DefaultValue {
    val age: Int? = 0
        get() = defaultValue(field, 0)

    val name: String? = ""
        get() = defaultValue(field, "")

}