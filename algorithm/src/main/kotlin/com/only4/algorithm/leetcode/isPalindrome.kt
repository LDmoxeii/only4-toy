package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun isPalindrome(head: ListNode?): Boolean {
    fun middleNode(node: ListNode): ListNode {
        var (slow, fast) = node to node as ListNode?
        while (fast?.next != null) {
            slow = slow.next!!
            fast = fast.next?.next
        }
        return slow
    }

    fun reverseList(head: ListNode?): ListNode? {
        tailrec fun dp(prev: ListNode?, curr: ListNode?): ListNode? {
            return when {
                curr == null -> prev
                else -> dp(curr, curr.next.also { curr.next = prev })
            }
        }

        return dp(null, head)
    }

    if (head == null) return true
    var head = head
    var mid: ListNode? = middleNode(head)
    mid = reverseList(mid)

    return generateSequence(head as ListNode? to mid) { (h, m) ->
        h?.next to m?.next
    }.takeWhile { (_, m) -> m != null }
        .all { (h, m) -> h?.`val` == m?.`val` }
}

