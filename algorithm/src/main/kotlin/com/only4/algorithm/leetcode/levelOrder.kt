package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun levelOrder(root: TreeNode?): List<List<Int>> {
    root ?: return emptyList()
    val deque = ArrayDeque<TreeNode>().apply { addLast(root) }
    val result = mutableListOf<MutableList<Int>>()

    do {
        val list = mutableListOf<Int>()
        repeat(deque.size) {
            deque.removeFirst().apply {
                left?.let { deque.addLast(it) }
                right?.let { deque.addLast(it) }
                list.add(this.`val`)
            }
        }
        result.add(list)
    } while (deque.isNotEmpty())
    return result
}

fun main() {
    val root = TreeNode(3).apply {
        left = TreeNode(9)
        right = TreeNode(20).apply {
            left = TreeNode(15)
            right = TreeNode(7)
        }
    }
    println(levelOrder(root))
}
