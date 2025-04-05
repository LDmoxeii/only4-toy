package com.only4.algorithm.leetcode

fun detectCycle(head: ListNode?): ListNode? {
    if (head?.next == null) return null
    var slow = head
    var fast = head
    var head = head
    while (fast?.next != null) {
        slow = slow?.next
        fast = fast.next?.next
        if (slow == fast) {
            while (slow != head) {
                slow = slow!!.next
                head = head!!.next
            }
            return head
        }
    }
    return null
}
