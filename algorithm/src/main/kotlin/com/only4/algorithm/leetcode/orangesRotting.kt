package com.only4.algorithm.leetcode

fun orangesRotting(grid: Array<IntArray>): Int {
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val plague = ArrayDeque<Pair<Int, Int>>()
    var fresh = 0

    grid.forEachIndexed { x, row ->
        row.forEachIndexed { y, value ->
            when (value) {
                1 -> fresh++
                2 -> plague.add(x to y)
            }
        }
    }

    if (fresh == 0) return 0

    var minutes = -1
    while (plague.isNotEmpty()) {
        repeat(plague.size) {
            val (x, y) = plague.removeFirst()
            directions.forEach { (dx, dy) ->
                val newX = x + dx
                val newY = y + dy
                if (newX in grid.indices && newY in grid[newX].indices && grid[newX][newY] == 1) {
                    grid[newX][newY] = 2
                    fresh--
                    plague.add(newX to newY)
                }
            }
        }
        minutes++
    }

    return if (fresh == 0) minutes else -1
}

