package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun detectCycle(head: ListNode?): ListNode? {
    var (slow, fast) = head to head
    while (fast?.next != null) {
        fast = fast.next!!.next
        slow = slow!!.next

        if (slow == fast) break
    }

    // fast 遇到空指针
    if (fast?.next == null) return null

    // 重新指向头节点
    slow = head

    // 快慢指针同步前进 相交点就是环起点
    while (slow != fast) {
        fast = fast!!.next
        slow = slow!!.next
    }

    return slow
}
