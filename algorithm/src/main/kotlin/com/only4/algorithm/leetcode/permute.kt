package com.only4.algorithm.leetcode

fun permute(nums: IntArray): List<List<Int>> {
    var result = mutableListOf<List<Int>>()
    val track = mutableListOf<Int>()
    val used = BooleanArray(nums.size) { false }
    fun backtrack() {
        if (track.size == nums.size) result.add(track.toList()).run { return }

        for (i in 0..<nums.size) {
            if (used[i]) continue
            track.add(nums[i]).run { used[i] = true }
            backtrack()
            track.removeLast().run { used[i] = false }
        }
    }
    backtrack()
    return result
}

fun main() {
    println(permute(intArrayOf(1, 2, 3)))
}
