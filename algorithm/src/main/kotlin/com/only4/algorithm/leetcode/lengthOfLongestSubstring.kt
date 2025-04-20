package com.only4.algorithm.leetcode

fun lengthOfLongestSubstring(s: String): Int {
    val window = mutableMapOf<Char, Int>().withDefault { 0 }
    var (left, right) = 0 to 0
    var res = 0
    while (right < s.length) {
        val c = s[right++]
        window.put(c, window.getValue(c) + 1)
        while (window[c]!! > 1) {
            val d = s[left++]
            window.put(d, window.getValue(d) - 1)
        }
        res = maxOf(res, right - left)
    }
    return res
}

fun main() {
    val s = " "
    println(lengthOfLongestSubstring(s))
}
