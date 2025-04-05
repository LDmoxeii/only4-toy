package com.only4.algorithm.leetcode

fun minWindow(s: String, t: String): String {
    var (finalLeft, finalRight) = -1 to s.length
    var left = 0

    var builder = mutableMapOf<Char, Int>().withDefault { 0 }
    val checker = t.groupingBy { it }.eachCount()

    s.forEachIndexed { right, it ->
        builder[it] = builder.getValue(it) + 1
        while (checker.all { (char, count) -> builder.getValue(char) >= count }) {
            if (finalRight - finalLeft > right - left) {
                finalRight = right
                finalLeft = left
            }
            builder.put(s[left], builder[s[left++]]!! - 1)
        }
    }

    return if (finalLeft < 0) "" else s.substring(finalLeft, finalRight + 1)
}

fun main() {
    val s = "A"
    val t = "AA"
    println(minWindow(s, t))
}
