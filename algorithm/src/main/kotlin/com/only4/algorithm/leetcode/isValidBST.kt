package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun isValidBST(root: TreeNode): Boolean {
    fun dp(root: TreeNode?, range: LongRange): Boolean {
        root ?: return true

        return root.left?.let { it.`val` in range.first..root.`val` - 1L } != false &&
                root.right?.let { it.`val` in root.`val` + 1L..range.last } != false &&
                dp(root.left, range.first..root.`val` - 1L) &&
                dp(root.right, root.`val` + 1L..range.last)
    }
    return dp(root, Long.MIN_VALUE + 1L..Long.MAX_VALUE - 1L)
}

fun main() {
    val root = TreeNode(-2147483648).apply {
        left = TreeNode(-2147483648)
    }
    println(-2147483648 in (Long.MIN_VALUE + 1..(-2147483648L - 1L)))
    println(isValidBST(root))
}
