package com.only4.algorithm.leetcode

fun maxSubArray(nums: IntArray): Int {
    var result = Int.MIN_VALUE
    val dp = IntArray(nums.size)

    nums.forEachIndexed { index, it ->
        when {
            index == 0 -> dp[index] = it.apply { result = maxOf(result, this) }

            else -> {
                dp[index] = maxOf(dp[index - 1], 0) + nums[index]
                result = maxOf(result, dp[index])
            }
        }
    }

    return result
}

fun main() {
    val nums = intArrayOf(5, 4, -1, 7, 8)
    println(maxSubArray(nums))
}
