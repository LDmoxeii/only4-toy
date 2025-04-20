package com.only4.algorithm.leetcode

fun solveNQueens(n: Int): List<List<String>> {
    val result = mutableListOf<List<String>>()
    val board = MutableList(n) { ".".repeat(n) }
    fun isValid(row: Int, col: Int): Boolean {
        repeat(row) { if (board[it][col] == 'Q') return false }
        var (x, y) = row - 1 to col + 1
        while (x >= 0 && y < board.size) {
            if (board[x--][y++] == 'Q') return false
        }
        x = row - 1;y = col - 1
        while (x >= 0 && y >= 0) {
            if (board[x--][y--] == 'Q') return false
        }
        return true
    }

    fun backtrack(row: Int) {
        if (row == board.size) result.add(board.toList()).run { return }

        val n = board[row].length
        repeat(n) { col ->
            if (!isValid(row, col)) return@repeat
            board[row] = board[row].toCharArray().apply { this[col] = 'Q' }.joinToString("")
            backtrack(row + 1)
            board[row] = board[row].toCharArray().apply { this[col] = '.' }.joinToString("")
        }
    }
    backtrack(0)
    return result
}

fun main() {
    val result = solveNQueens(1)
    println(result)
}
