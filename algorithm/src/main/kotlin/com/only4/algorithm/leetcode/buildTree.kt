package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun buildTree(preorder: IntArray, inorder: IntArray): TreeNode? {
    if (preorder.isEmpty()) return null

    return TreeNode(preorder[0]).apply {
        val inorderIndex = inorder.indexOf(preorder[0])
        left = buildTree(
            preorder = preorder.copyOfRange(1, inorderIndex + 1),
            inorder = inorder.copyOfRange(0, inorderIndex)
        )
        right = buildTree(
            preorder = preorder.copyOfRange(inorderIndex + 1, preorder.size),
            inorder = inorder.copyOfRange(inorderIndex + 1, inorder.size)
        )
    }
}

fun main() {
    val preorder = intArrayOf(1, 2, 3)
    val inorder = intArrayOf(3, 2, 1)
    val root = buildTree(preorder, inorder)
}
