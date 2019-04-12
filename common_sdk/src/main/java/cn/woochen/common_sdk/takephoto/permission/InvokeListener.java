package cn.woochen.common_sdk.takephoto.permission;


import cn.woochen.common_sdk.takephoto.model.InvokeParam;

/**
 * 授权管理回调
 */
public interface InvokeListener {
    PermissionManager.TPermissionType invoke(InvokeParam invokeParam);
}
