package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun lowestCommonAncestor(root: TreeNode?, p: TreeNode?, q: TreeNode?): TreeNode? = when {
    root == null || root == p || root == q -> root
    else -> {
        val left = lowestCommonAncestor(root.left, p, q)
        val right = lowestCommonAncestor(root.right, p, q)
        when {
            left != null && right != null -> root
            else -> left ?: right
        }
    }
}
