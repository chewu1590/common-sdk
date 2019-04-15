package cn.woochen.commonsdk.samples.bean

interface DefaultValue {
     fun <T : Any> defaultValue(any: T?, default: T) = any ?: default
}