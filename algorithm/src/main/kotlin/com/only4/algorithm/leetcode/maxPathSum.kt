package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun maxPathSum(root: TreeNode): Int {
    var result = Int.MIN_VALUE
    fun TreeNode?.calculateMaxGain(): Int = this?.run {
        val leftGain = left.calculateMaxGain().coerceAtLeast(0)
        val rightGain = right.calculateMaxGain().coerceAtLeast(0)

        result = maxOf(result, leftGain + rightGain + `val`)
        `val` + maxOf(leftGain, rightGain)
    } ?: 0
    root.calculateMaxGain()
    return result
}
