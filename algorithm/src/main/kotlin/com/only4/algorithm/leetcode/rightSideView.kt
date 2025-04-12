package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun rightSideView(root: TreeNode?): List<Int> {
    root ?: return emptyList()
    val stack = ArrayDeque<TreeNode>().apply { addFirst(root) }
    val result = mutableListOf<Int>()

    do {
        result.add(stack.first().`val`)
        repeat(stack.size) {
            stack.removeLast().apply {
                left?.let { stack.addFirst(it) }
                right?.let { stack.addFirst(it) }
            }
        }
    } while (stack.isNotEmpty())
    return result
}

fun main() {
    val root = TreeNode(1).apply {
        left = TreeNode(2).apply {
            right = TreeNode(5)
        }
        right = TreeNode(3).apply {
            right = TreeNode(4)
        }
    }
    rightSideView(root)
}
