package com.liuyue.daydaybook.help.permission

import android.content.Intent

interface OnRequestPermissionsResultCallback {

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}
