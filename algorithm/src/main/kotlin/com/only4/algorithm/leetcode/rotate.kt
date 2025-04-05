package com.only4.algorithm.leetcode

fun rotate(nums: IntArray, k: Int): Unit {
    fun IntArray.reverseSub(start: Int, end: Int) {
        var (left, right) = start to end
        while (left < right) this[left] = this[right].also { this[right--] = this[left++] }
    }

    val mod = k % nums.size
    nums.reverseSub(0, nums.lastIndex)
    nums.reverseSub(0, mod - 1)
    nums.reverseSub(mod, nums.lastIndex)

}

fun rotate(matrix: Array<IntArray>): Unit {
    var (top, bottom, left, right) = listOf(0, matrix.lastIndex, 0, matrix.lastIndex)


    while (top < bottom) {
        val steps = right - left
        for (offset in 0 until steps) {
            val temp = matrix[top][left + offset]

            matrix[top][left + offset] = matrix[bottom - offset][left]
            matrix[bottom - offset][left] = matrix[bottom][right - offset]
            matrix[bottom][right - offset] = matrix[top + offset][right]
            matrix[top + offset][right] = temp
        }
        left++
        right--
        top++
        bottom--
    }
}

fun main() {
    val matrix = arrayOf(
        intArrayOf(1, 2, 3),
        intArrayOf(4, 5, 6),
        intArrayOf(7, 8, 9)
    )
    rotate(matrix)
    matrix.forEach { println(it.joinToString()) }
}
