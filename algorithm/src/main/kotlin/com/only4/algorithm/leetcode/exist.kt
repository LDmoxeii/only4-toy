package com.only4.algorithm.leetcode

fun exist(board: Array<CharArray>, word: String): Boolean {
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    fun dfs(x: Int, y: Int, target: Int): Boolean {
        if (board[x][y] != word[target]) return false
        if (target == word.lastIndex) return true
        board[x][y] = '0'
        directions.forEach { (dx, dy) ->
            if (x + dx in board.indices && y + dy in board[x].indices && dfs(x + dx, y + dy, target + 1)) return true
        }
        board[x][y] = word[target]
        return false
    }

    board.forEachIndexed { x, rows ->
        rows.forEachIndexed { y, `val` ->
            if (dfs(x, y, 0)) return true
        }
    }
    return false
}

fun main() {
    println(
        exist(
            arrayOf(
                charArrayOf('A', 'B', 'C', 'E'),
                charArrayOf('S', 'F', 'C', 'S'),
                charArrayOf('A', 'D', 'E', 'E')
            ), "SEE"
        )
    )
}
