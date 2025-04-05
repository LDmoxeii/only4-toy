package com.only4.algorithm.leetcode

fun getIntersectionNode(headA:ListNode?, headB:ListNode?):ListNode? {
    var (nodeA, nodeB) = headA to headB
    while (nodeA !== nodeB) {
        nodeA = if (nodeA == null) headB
        else nodeA.next

        nodeB = if (nodeB == null) headA
        else nodeB.next
    }
    return nodeA
}
