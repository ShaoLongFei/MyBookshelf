package com.liuyue.daydaybook.help.permission

interface OnPermissionsDeniedCallback {
    fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>)
}
