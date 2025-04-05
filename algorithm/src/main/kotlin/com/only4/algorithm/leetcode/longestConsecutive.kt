package com.only4.algorithm.leetcode

import kotlin.math.max

fun longestConsecutive(nums: IntArray): Int {
    var result = 0
    val ints = nums.toHashSet()
    ints.forEach {
        if (ints.contains(it - 1)) return@forEach
        var length = 0
        var point = it
        while (ints.contains(point++)) {
            length++
        }
        result = max(result, length)
    }
    return result
}

fun main() {
    val nums = intArrayOf(100,4,200,1,3,2)
    println(longestConsecutive(nums))
}
