package com.only4.iterator.utils

import com.only4.iterator.entity.User
import java.io.BufferedReader

class UserFileReader(
    private val bufferedReader: BufferedReader
) : Iterable<User> {
    override fun iterator(): Iterator<User> {
        return object : Iterator<User> {

            private val users by lazy {
                val users = mutableListOf<User>()
                bufferedReader.useLines {
                    it.forEach {
                        val (id, name, age) = it.substring(1, it.length - 1).split(",")
                        users.add(User(id.toInt(), name, age.toInt()))
                    }
                }
                users
            }
            private var cursor = 0

            override fun next(): User {
                if (cursor >= users.size) throw NoSuchElementException()
                val currentIndex = cursor
                cursor++
                return users[currentIndex]
            }

            override fun hasNext(): Boolean {
                return cursor != users.size
            }
        }
    }
}
