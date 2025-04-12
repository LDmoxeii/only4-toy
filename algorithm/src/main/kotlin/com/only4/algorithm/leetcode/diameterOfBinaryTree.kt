package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun diameterOfBinaryTree(root: TreeNode?): Int {
    root ?: return 0
    var result = 0
    fun maxDepth(root: TreeNode?): Int {
        root ?: return 0
        val left = maxDepth(root.left)
        val right = maxDepth(root.right)
        result = maxOf(result, left + right)
        return maxOf(left, right) + 1
    }
    maxDepth(root)
    return result
}

fun main() {
    val root = TreeNode(4).apply {
        left = TreeNode(-7)
        right = TreeNode(-3).apply {
            left = TreeNode(-9).apply {
                left = TreeNode(9).apply {
                    left = TreeNode(6).apply {
                        left = TreeNode(0).apply {
                            right = TreeNode(-1)
                        }
                        right = TreeNode(6).apply {
                            left = TreeNode(-4)
                        }
                    }
                }
                right = TreeNode(-7).apply {
                    left = TreeNode(-6).apply {
                        left = TreeNode(5)
                    }
                    right = TreeNode(-6).apply {
                        left = TreeNode(9)
                        right = TreeNode(-2)
                    }
                }
            }
            right = TreeNode(-3).apply {
                left = TreeNode(-4)
            }
        }
    }
    println(diameterOfBinaryTree(root))
}
