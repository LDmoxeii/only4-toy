package com.only4.algorithm.leetcode

fun subarraySum(nums: IntArray, k: Int): Int {
    var count = 0
    val sum = mutableMapOf<Int, Int>().withDefault { 0 }
    nums.forEachIndexed { i, x ->
        sum[i] = sum.getValue(i - 1) + nums[i]
    }
    val cache = mutableMapOf<Int, Int>().withDefault { 0 }

    (-1 until sum.size).forEach {
        val right = sum.getValue(it)
        val left = right - k
        count += cache.getValue(left)
        cache[right] = cache.getValue(right) + 1
    }

    return count
}

fun main() {
    println(subarraySum(intArrayOf(1, 1, 1), 2))
    println(subarraySum(intArrayOf(1, 2, 3), 3))
    println(subarraySum(intArrayOf(-1, -1, 1), 0))
}
