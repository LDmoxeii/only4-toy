package com.only4.algorithm.leetcode

class Trie {
    private val root = Node()

    private data class Node(
        val children: MutableMap<Char, Node> = mutableMapOf(),
        var isTerminal: Boolean = false
    )

    fun insert(word: String) {
        word.fold(root) { node, char ->
            node.children.getOrPut(char) { Node() }
        }.isTerminal = true
    }

    fun search(word: String): Boolean = traverse(word)?.isTerminal == true

    fun startsWith(prefix: String): Boolean = traverse(prefix) != null

    private fun traverse(input: String): Node? = input.fold(root) { node, char ->
        node.children[char] ?: return null
    }
}
