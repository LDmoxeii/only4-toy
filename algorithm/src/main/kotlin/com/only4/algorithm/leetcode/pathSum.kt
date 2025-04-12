package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun pathSum(root: TreeNode?, targetSum: Int): Int = root?.run {
    fun dfs(node: TreeNode?, target: Long): Int = node?.run {
        (`val`.toLong() == target).toInt() + dfs(left, target - `val`) + dfs(right, target - `val`)
    } ?: 0

    dfs(this, targetSum.toLong()) + pathSum(left, targetSum) + pathSum(right, targetSum)
} ?: 0

private fun Boolean.toInt() = if (this) 1 else 0
