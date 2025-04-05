package com.only4.algorithm.leetcode

fun trap(heights: IntArray): Int {
    var totalWater = 0
    var (leftWall, rightWall) = heights.first() to heights.last()
    var (left, right) = 0 to heights.lastIndex

    while (left < right) {
        val lowWall = minOf(leftWall, rightWall)
        when {
            leftWall < rightWall -> {
                val basin = heights[left++]
                val water = lowWall - basin
                totalWater += maxOf(water, 0)
                leftWall = maxOf(leftWall, heights[left])
            }
            else -> {
                val basin = heights[right--]
                val water = lowWall - basin
                totalWater += maxOf(water, 0)
                rightWall = maxOf(rightWall, heights[right])
            }
        }
    }

    return totalWater
}

fun main() {
    val result = trap(intArrayOf(0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1))
    println(result)
}
