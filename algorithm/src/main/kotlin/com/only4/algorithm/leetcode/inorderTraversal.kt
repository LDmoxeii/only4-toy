package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun inorderTraversal(root: TreeNode?): List<Int> {
    val result = mutableListOf<Int>()
    fun inorderTraversal(root: TreeNode?) {
        root ?: return

        inorderTraversal(root.left)
        result.add(root.`val`)
        inorderTraversal(root.right)
    }
    inorderTraversal(root)
    return result
}

fun main() {
    val root = TreeNode(1).apply {
        right = TreeNode(2).apply {
            left = TreeNode(3)
        }
    }
    println(inorderTraversal(root))
}
