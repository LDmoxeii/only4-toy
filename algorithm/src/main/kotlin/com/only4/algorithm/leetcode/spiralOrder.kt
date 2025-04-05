package com.only4.algorithm.leetcode

fun spiralOrder(matrix: Array<IntArray>): List<Int> {
    if (matrix.isEmpty()) return emptyList()

    var (top, bottom, left, right) = listOf(0, matrix.lastIndex, 0, matrix[0].lastIndex)
    val result = mutableListOf<Int>()

    while (top <= bottom && left <= right) {
        // 1. 从左到右遍历上边界
        (left..right).forEach { result.add(matrix[top][it]) }
        top++

        // 2. 从上到下遍历右边界
        (top..bottom).forEach { result.add(matrix[it][right]) }
        right--

        // 3. 从右到左遍历下边界（如果存在剩余行）
        if (top <= bottom) {
            (right downTo left).forEach { result.add(matrix[bottom][it]) }
            bottom--
        }

        // 4. 从下到上遍历左边界（如果存在剩余列）
        if (left <= right) {
            (bottom downTo top).forEach { result.add(matrix[it][left]) }
            left++
        }
    }

    return result
}

fun main() {
    val matrix = arrayOf(
        intArrayOf(1, 2, 3),
        intArrayOf(4, 5, 6),
        intArrayOf(7, 8, 9)
    )
    println(spiralOrder(matrix))
}
