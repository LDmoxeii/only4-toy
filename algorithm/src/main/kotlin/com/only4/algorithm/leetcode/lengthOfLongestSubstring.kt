package com.only4.algorithm.leetcode

fun lengthOfLongestSubstring(s: String): Int {
    var maxLength = 0
    val cache = mutableSetOf<Char>()
    var left = 0

    s.forEachIndexed { right, it ->
        while (cache.contains(it)) {
            maxLength = maxOf(maxLength, cache.size)
            cache.remove(s[left++])
        }
        cache.add(it)
    }
    maxLength = maxOf(maxLength, cache.size)
    return maxLength
}

fun main() {
    val s = " "
    println(lengthOfLongestSubstring(s))
}
