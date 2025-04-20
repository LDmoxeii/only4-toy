package com.only4.algorithm.leetcode

fun numIslands(grid: Array<CharArray>): Int {
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    fun dfs(x: Int, y: Int) {
        if (x !in grid.indices || y !in grid[x].indices || grid[x][y] != '1') return
        grid[x][y] = '0'
        directions.forEach { (dx, dy) -> dfs(x + dx, y + dy) }
    }

    return grid.indices.sumOf { x ->
        grid[x].indices.count { y ->
            (grid[x][y] == '1').also { if (it) dfs(x, y) }
        }
    }
}
