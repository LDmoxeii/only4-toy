package com.only4.algorithm.leetcode

fun twoSum(nums: IntArray, target: Int): IntArray {
    val map = mutableMapOf<Int, Int>()

    nums.forEachIndexed { index, it ->
        val diff = target - it
        if (map.contains(it)) return intArrayOf(map[it]!!, index)
        map[diff] = index
    }
    throw IllegalArgumentException("No two sum solution")
}

fun main() {
    val nums = intArrayOf(2, 7, 11, 15)
    val target = 9
    val result = twoSum(nums, target)
    println(result.joinToString())
}
