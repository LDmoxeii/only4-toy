package com.only4.proxy.handler

import com.only4.proxy.intf.SimpleInterface

interface SimpleHandler {

    fun functionBody(methodName: String): String

    fun setProxy(proxy: SimpleInterface) {}
}
