package com.only4.algorithm.leetcode

fun minWindow(s: String, t: String): String {
    val checker = mutableMapOf<Char, Int>().withDefault { 0 }
    val window = mutableMapOf<Char, Int>().withDefault { 0 }
    t.forEach { checker.put(it, checker.getValue(it) + 1) }

    var (left, right) = 0 to 0
    var valid = 0
    var (start, len) = 0 to Int.MAX_VALUE
    while (right < s.length) {
        val c = s[right++]
        if (checker.containsKey(c)) {
            window.put(c, window.getValue(c) + 1)
            if (window[c] == checker[c]) valid++
        }

        while (valid == checker.size) {
            if (right - left < len)
                len = right - left.also { start = it }
            val d = s[left++]
            if (checker.containsKey(d)) {
                if (window[d] == checker[d]) valid--
                window.put(d, window.getValue(d) - 1)
            }
        }
    }
    return if (len == Int.MAX_VALUE) "" else s.substring(start, start + len)
}

fun main() {
    val s = "ADOBECODEBANC"
    val t = "ABC"
    println(minWindow(s, t))
}
