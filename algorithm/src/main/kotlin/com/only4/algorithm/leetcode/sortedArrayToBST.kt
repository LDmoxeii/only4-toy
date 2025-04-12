package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.TreeNode

fun sortedArrayToBST(nums: IntArray): TreeNode? {
    fun dp(start: Int, end: Int): TreeNode? {
        if (start > end) return null
        val mid = (end + start) / 2
        return TreeNode(nums[mid]).apply {
            left = dp(start, mid - 1)
            right = dp(mid + 1, end)
        }
    }
    return dp(0, nums.lastIndex)
}

fun main() {
    val nums = intArrayOf(-10, -3, 0, 5, 9)
    println(sortedArrayToBST(nums))
}
