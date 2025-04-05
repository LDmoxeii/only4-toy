package com.only4.algorithm.leetcode

fun merge(intervals: Array<IntArray>): Array<IntArray> {
    intervals.sortBy { it[0] }
    var result = mutableListOf<IntArray>()
    intervals.forEach {
        when {
            result.isNotEmpty() && it[0] <= result.last()[1] -> result.last()[1] = maxOf(result.last()[1], it[1])
            else -> result.add(it)
        }
    }
    return result.toTypedArray()
}

fun main() {
    val intervals = arrayOf(
        intArrayOf(2, 3),
        intArrayOf(4, 5),
        intArrayOf(6, 7),
        intArrayOf(8, 9),
    )
    merge(intervals)
}
