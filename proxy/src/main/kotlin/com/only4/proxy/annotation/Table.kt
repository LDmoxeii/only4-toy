package com.only4.proxy.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Table(
    val tableName: String = ""
)
