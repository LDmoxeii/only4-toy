package com.only4.algorithm.leetcode

fun generateParenthesis(n: Int): List<String> {
    var result = mutableListOf<String>()
    fun dfs(index: Int, open: Int, path: StringBuilder) {

        if (index == n * 2) result.add(path.toString()).run { return }

        if (open < n) dfs(index + 1, open + 1, path.append("(")).run { path.deleteAt(path.lastIndex) }

        if (index - open < open) dfs(index + 1, open, path.append(")")).run { path.deleteAt(path.lastIndex) }
    }
    dfs(0, 0, StringBuilder())
    return result
}

fun main() {
    println(generateParenthesis(3))
}
