package com.only4.algorithm.leetcode

fun twoSum(numbers: IntArray, target: Int): IntArray {
    var (left, right) = 0 to numbers.lastIndex
    while (left < right) {
        val sum = numbers[left] + numbers[right]
        when {
            sum == target -> return return intArrayOf(left + 1, right + 1)
            sum < target -> left++
            else -> right--
        }
    }
    throw IllegalArgumentException("No two sum solution")
}

fun main() {
    val nums = intArrayOf(2, 7, 11, 15)
    val target = 9
    val result = twoSum(nums, target)
    println(result.joinToString())
}
