package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun swapPairs(head: ListNode?): ListNode? {
    if (head?.next == null) return head

    val node1 = head /* 头 */
    val node2 = node1.next!! /* 尾 */
    val node3 = node2.next /* 下一组的头 */

    /* 头 变 尾 -> 下一组的头 */ node1.next = swapPairs(node3)
    /* 尾 变 头 -> 尾 */ node2.next = node1

    return node2 /* 头 */
}
