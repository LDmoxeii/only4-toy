package com.only4.algorithm.leetcode

fun hasCycle(head: ListNode?): Boolean {
    if (head?.next == null) return false
    var (slow, fast) = head as ListNode? to head as ListNode?
    do {
        if (fast?.next == null && slow != null) return false
        slow  = slow?.next
        fast = fast?.next?.next
    } while (slow !== fast)
    return true
}
