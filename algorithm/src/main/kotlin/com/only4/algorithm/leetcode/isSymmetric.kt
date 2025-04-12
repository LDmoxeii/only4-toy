package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun isSymmetric(root: TreeNode?): Boolean {
    root ?: return true
    fun dp(left: TreeNode?, right: TreeNode?): Boolean {
        if (left == null && right == null) return true

        return left?.`val` == right?.`val` && dp(left?.left, right?.right) && dp(left?.right, right?.left)
    }
    return dp(root.left, root.right)
}

fun main() {
    val root = TreeNode(1).apply {
        left = TreeNode(2).apply {
            left = TreeNode(3)
            right = TreeNode(4)
        }
        right = TreeNode(2).apply {
            left = TreeNode(4)
            right = TreeNode(3)
        }
    }
    isSymmetric(root)
}
