package com.only4.proxy.mapper

import com.only4.proxy.annotation.Param
import com.only4.proxy.entity.User

interface UserMapper {
    fun getUserById(@Param("id") id: Int): User
    fun getUserByName(@Param("name") name: String): User
    fun getUserByNameAndAge(@Param("name") name: String, @Param("age") age: Int): User
}
