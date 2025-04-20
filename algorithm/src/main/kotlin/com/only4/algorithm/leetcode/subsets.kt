package com.only4.algorithm.leetcode

fun subsets(nums: IntArray): List<List<Int>> {
    var result = mutableListOf<List<Int>>()
    val track = mutableListOf<Int>()
    fun backtrack(index: Int) {
        result.add(track.toList())

        for (i in index..<nums.size) {
            track.add(nums[i])
            backtrack(i + 1)
            track.removeLast()
        }
    }
    backtrack(0)
    return result
}

fun main() {
    println(subsets(intArrayOf(1, 2, 3)))
}
