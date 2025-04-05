package com.only4.proxy.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param(
    val value: String = ""
)
