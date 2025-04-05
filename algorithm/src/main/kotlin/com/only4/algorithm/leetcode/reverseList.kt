package com.only4.algorithm.leetcode

fun reverseList(head: ListNode?): ListNode? {
    tailrec fun dp (prev: ListNode?, curr: ListNode?): ListNode? {
        return when {
            curr == null -> prev
            else -> dp(curr, curr.next.also { curr.next = prev })
        }
    }

    return dp(null, head)
}
