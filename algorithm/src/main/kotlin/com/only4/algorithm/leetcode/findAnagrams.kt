package com.only4.algorithm.leetcode

fun findAnagrams(s: String, p: String): List<Int> {
    val result = mutableListOf<Int>()
    val checker = mutableMapOf<Char, Int>().withDefault { 0 }
    val window = mutableMapOf<Char, Int>().withDefault { 0 }
    p.forEach { checker.put(it, checker.getValue(it) + 1) }

    var (left, right) = 0 to 0
    var valid = 0
    while (right < s.length) {
        val c = s[right++]
        if (checker.containsKey(c)) {
            window.put(c, window.getValue(c) + 1)
            if (window[c] == checker[c]) valid++
        }
        while (right - left >= p.length) {
            if (valid == checker.size) result.add(left)
            val d = s[left++]
            if (checker.containsKey(d)) {
                if (window[d] == checker[d]) valid--
                window.put(d, window.getValue(d) - 1)
            }
        }
    }
    return result
}

fun main() {
    val s = "baa"
    val p = "aa"
    println(findAnagrams(s, p))
}
