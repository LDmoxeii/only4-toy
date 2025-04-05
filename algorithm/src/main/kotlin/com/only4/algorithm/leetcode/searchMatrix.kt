package com.only4.algorithm.leetcode

fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {
    val (m, n) = matrix.size to matrix[0].size
    var (x, y) = Pair(0, n - 1)

    while (x < m && y > -1) {
        when {
            matrix[x][y] == target -> return true

            matrix[x][y] > target -> y--

            matrix[x][y] < target -> x++
        }
    }
    return false
}

fun main() {
    val matrix = arrayOf(
        intArrayOf(1, 3, 5, 7),
        intArrayOf(10, 11, 16, 20),
        intArrayOf(23, 30, 34, 60)
    )
    println(searchMatrix(matrix, 3))
}
