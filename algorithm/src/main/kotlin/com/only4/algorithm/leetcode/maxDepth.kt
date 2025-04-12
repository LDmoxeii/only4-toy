package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun maxDepth(root: TreeNode?): Int {
    root ?: return 0

    return maxOf(maxDepth(root.left), maxDepth(root.right)) + 1
}

fun main() {
    val root = TreeNode(1).apply { left = TreeNode(2) }
    println(maxDepth(root))
}
