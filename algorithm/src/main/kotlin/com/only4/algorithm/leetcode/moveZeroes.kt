package com.only4.algorithm.leetcode

fun moveZeroes(nums: IntArray): Unit {
    var zeroPoint = 0

    nums.forEachIndexed { nonZeroPoint, it ->
        if (nums[nonZeroPoint] != 0) {
            nums[zeroPoint] = nums[nonZeroPoint].also { nums[nonZeroPoint] = nums[zeroPoint] }
            zeroPoint++
        }
    }
}

fun main() {
    val nums = intArrayOf(0, 1, 0, 3, 12)
    val case1 = intArrayOf(1, 0, 1)
    moveZeroes(case1)
}
