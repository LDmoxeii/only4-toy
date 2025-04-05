package com.only4.algorithm.leetcode

fun setZeroes(matrix: Array<IntArray>): Unit {
    var zeroX = mutableSetOf<Int>()
    var zeroY = mutableSetOf<Int>()

    matrix.forEachIndexed { x, row ->
        row.forEachIndexed { y, it ->
            if (it == 0) {
                zeroX.add(x)
                zeroY.add(y)
            }
        }
    }
    matrix.forEachIndexed { x, row ->
        row.forEachIndexed { y, it ->
            if (zeroX.contains(x) || zeroY.contains(y)) matrix[x][y] = 0
        }
    }
}



fun main() {
    val matrix = arrayOf(
        intArrayOf(1, 1, 1),
        intArrayOf(1, 0, 1),
        intArrayOf(1, 1, 1)
    )

    setZeroes(matrix)

    matrix.forEach {
        println(it.joinToString())
    }
}
