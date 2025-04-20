package com.only4.algorithm.leetcode

fun combinationSum(candidates: IntArray, target: Int): List<List<Int>> {
    candidates.sort()
    val track = mutableListOf<Int>()
    var trackSum = 0
    val result = mutableListOf<List<Int>>()
    fun backtrack(start: Int) {
        if (trackSum == target) result.add(track.toList())
        if (trackSum > target) return

        for (index in start..<candidates.size) {
            track.add(candidates[index].also { trackSum += it })
            backtrack(index)
            track.removeLast().also { trackSum -= it }
        }
    }
    backtrack(0)
    return result
}

fun main() {
    println(combinationSum(intArrayOf(2, 3, 6, 7), 7))
}
