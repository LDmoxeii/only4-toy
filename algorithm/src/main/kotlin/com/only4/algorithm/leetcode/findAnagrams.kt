package com.only4.algorithm.leetcode

fun findAnagrams(s: String, p: String): List<Int> {
    val result = mutableListOf<Int>()
    val checker = IntArray(26)
    val cache = IntArray(26)
    p.forEach { checker[it - 'a']++ }

    s.forEachIndexed { right, it ->
        cache[it - 'a']++
        val left = right - p.length + 1
        if (left < 0) return@forEachIndexed
        if (cache.contentEquals(checker)) result.add(left)
        cache[s[left] - 'a']--
    }
    return result
}

fun main() {
    val s = "baa"
    val p = "aa"
    println(findAnagrams(s, p))
}
