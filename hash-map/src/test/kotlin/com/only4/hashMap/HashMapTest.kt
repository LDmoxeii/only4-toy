package com.only4.hashMap

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class HashMapTest {
    @Test
    fun testApi() {
        val myHashMap: java.util.HashMap<String, String> = java.util.HashMap()
        val count = 10000
        for (i in 0..<count) {
            myHashMap.put(i.toString(), i.toString())
        }

        assertEquals(count, myHashMap.size)

        for (i in 0..<count) {
            assertEquals(i.toString(), myHashMap.get(i.toString()))
        }

        myHashMap.remove("8")
        assertNull(myHashMap.get("8"))

        assertEquals(count - 1, myHashMap.size)
    }
}

sealed interface ReadResult
data class Number(val number: Int) : ReadResult
data class Text(val text: String) : ReadResult
data object EndOfFile

fun main() {
    println(Number(7))
    // Number(number=7)
    println(EndOfFile)
    // EndOfFile
}
