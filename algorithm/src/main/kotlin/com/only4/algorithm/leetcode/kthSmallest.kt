package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun kthSmallest(root: TreeNode?, k: Int): Int {
    var deque = ArrayDeque<Int>()
    fun dp(root: TreeNode?) {
        root ?: return
        dp(root.left)
        if (deque.size == k) return
        deque.addFirst(root.`val`)
        dp(root.right)
    }
    dp(root)
    return deque.first()
}

fun main() {
    val root = TreeNode(5).apply {
        left = TreeNode(3).apply {
            left = TreeNode(2).apply {
                left = TreeNode(1)
            }
            right = TreeNode(4)
        }
        right = TreeNode(6)
    }
    println(kthSmallest(root, 3))
}
