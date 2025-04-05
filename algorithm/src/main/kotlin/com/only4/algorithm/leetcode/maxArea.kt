package com.only4.algorithm.leetcode

fun maxArea(height: IntArray): Int {
    var result = 0
    var (left, right) = 0 to height.lastIndex
    while (left < right) {
        result = maxOf(result, minOf(height[left], height[right]) * right - left)
        if (height[left] < height[right]) left++
        else right--
    }

    return result
}

fun main() {
    val height = intArrayOf(1, 8, 6, 2, 5, 4, 8, 3, 7)
    val case1 = intArrayOf(1, 1)
    println(maxArea(case1))
}
