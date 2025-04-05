package com.only4.proxy.entity

import com.only4.proxy.annotation.Table

@Table(tableName = "users")
data class User(
    val id: Int = 0,
    val name: String = "",
    val age: Int = 0,
)
