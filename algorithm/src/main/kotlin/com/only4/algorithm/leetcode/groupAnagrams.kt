package com.only4.algorithm.leetcode

fun groupAnagrams(strs: Array<String>): List<List<String>> {
    val map = mutableMapOf<String, MutableList<String>>()

    strs.forEach {
        val key = it.toCharArray().sorted().joinToString("")
        if (map.contains(key)) map[key]!!.add(it)
        else map[key] = mutableListOf(it)
    }
    return map.values.toList()
}

fun main() {
    val strs = arrayOf("ddddddddddg","dgggggggggg")
    println(groupAnagrams(strs))
}
