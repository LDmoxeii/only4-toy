package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun flatten(root: TreeNode?): TreeNode? {
    root ?: return null

    val left = flatten(root.left)
    val right = flatten(root.right)
    left?.let {
        it.right = root.right
        root.right = root.left
        root.left = null
    }
    return right ?: left ?: root
}

fun main() {
    val root = TreeNode(1).apply {
        left = TreeNode(2).apply {
            left = TreeNode(3)
            right = TreeNode(4)
        }
        right = TreeNode(5).apply {
            right = TreeNode(6)
        }
    }
    flatten(root)
}
