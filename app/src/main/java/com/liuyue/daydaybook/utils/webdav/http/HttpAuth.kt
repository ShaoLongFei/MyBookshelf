package com.liuyue.daydaybook.utils.webdav.http

object HttpAuth {

    var auth: Auth? = null

    class Auth internal constructor(val user: String, val pass: String)

}