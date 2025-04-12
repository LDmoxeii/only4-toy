package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun invertTree(root: TreeNode?): TreeNode? {
    root ?: return null

    val left = invertTree(root.left)
    val right = invertTree(root.right)
    with(root) {
        this.left = right
        this.right = left
    }
    return root
}

fun main() {
    val root = TreeNode(4).apply {
        left = TreeNode(2).apply {
            left = TreeNode(1)
            right = TreeNode(3)
        }
        right = TreeNode(7).apply {
            left = TreeNode(6)
            right = TreeNode(9)
        }
    }
    println(invertTree(root))
}
