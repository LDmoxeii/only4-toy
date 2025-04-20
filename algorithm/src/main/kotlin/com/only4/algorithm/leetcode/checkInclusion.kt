package com.only4.algorithm.leetcode

fun checkInclusion(s1: String, s2: String): Boolean {
    val checker = mutableMapOf<Char, Int>().withDefault { 0 }
    val window = mutableMapOf<Char, Int>().withDefault { 0 }
    s1.forEach { checker.put(it, checker.getValue(it) + 1) }

    var (left, right) = 0 to 0
    var valid = 0
    while (right < s2.length) {
        val c = s2[right++]
        if (checker.containsKey(c)) {
            window.put(c, window.getValue(c) + 1)
            if (window[c] == checker[c]) valid++
        }

        while (right - left >= s1.length) {
            if (valid == checker.size) return true
            val d = s2[left++]
            if (checker.containsKey(d)) {
                if (window[d] == checker[d]) valid--
                window.put(d, window.getValue(d) - 1)
            }
        }
    }
    return false
}
