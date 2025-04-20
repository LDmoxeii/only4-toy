package com.only4.algorithm.leetcode

fun letterCombinations(digits: String): List<String> {
    if (digits.isEmpty()) return emptyList()

    val keyboard = arrayOf(
        "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
    )
    val result = mutableListOf<String>()
    val track = StringBuilder()
    fun backtrack(start: Int) {
        if (track.length == digits.length) result.add(track.toString()).run { return }

        for (char in keyboard[digits[start].digitToInt()]) {
            track.append(char)
            backtrack(start + 1)
            track.deleteAt(track.lastIndex)
        }
    }

    backtrack(0)
    return result
}

fun main() {
    println(letterCombinations("23"))
}
