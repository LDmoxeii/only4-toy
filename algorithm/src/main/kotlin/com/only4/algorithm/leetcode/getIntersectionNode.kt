package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun getIntersectionNode(headA:ListNode?, headB:ListNode?):ListNode? {
    var (pA, pB) = headA to headB
    while (pA !== pB) {
        // pA 走一步 走到末尾 链接 B链表
        pA = if (pA == null) headB
             else            pA.next

        // pB 走一步 走到末尾 链接 A链表
        pB = if (pB == null) headA
             else            pB.next
    }
    return pA
}
