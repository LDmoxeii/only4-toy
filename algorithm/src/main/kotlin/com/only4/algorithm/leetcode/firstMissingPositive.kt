package com.only4.algorithm.leetcode

import kotlin.math.absoluteValue

fun firstMissingPositive(nums: IntArray): Int {

    if (1 !in nums) return 1

    nums.indices.forEach { i ->
        nums[i] = nums[i].takeIf { it in 1..nums.size } ?: 1
    }

    nums.indices.forEach { i ->
        val num = nums[i].absoluteValue
        nums[num - 1] = -nums[num - 1].absoluteValue
    }

    return nums.indexOfFirst { it > 0 }
        .takeIf { it != -1 }
        ?.plus(1)
        ?: (nums.size + 1)
}

fun main() {
    println(firstMissingPositive(intArrayOf(3, 4, -1, 1)))
}
