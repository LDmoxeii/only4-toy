package com.only4.algorithm.leetcode

fun productExceptSelf(nums: IntArray): IntArray {
    var left = 1
    val result = IntArray(nums.size).apply {
        nums.indices.forEach { i -> this[i] = left.also { left *= nums[i] } }
    }

    var right = 1
    nums.indices.reversed().forEach { i -> result[i] *= right.also { right *= nums[i] } }

    return result
}

fun main() {
    val result = productExceptSelf(intArrayOf(1, 2, 3, 4))
    result.forEach {
        println(it)
    }
}

