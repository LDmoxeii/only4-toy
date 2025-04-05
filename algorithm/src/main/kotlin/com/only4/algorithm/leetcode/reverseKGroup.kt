package com.only4.algorithm.leetcode

fun reverseKGroup(head: ListNode?, k: Int): ListNode? {
    val dummy = ListNode(0).apply { next = head }
    var counter = head
    var count = 0
    while (counter.also { counter = counter?.next } != null) count++
    var p0 = dummy
    repeat(count / k) {
        var prev: ListNode? = null
        var curr: ListNode? = p0.next!!
        repeat(k) {
            var next = curr!!.next
            curr.next = prev

            prev = curr
            curr = next
        }
        val next = p0.next
        p0.next!!.next = curr
        p0.next = prev
        p0 = next!!
    }
    return dummy.next
}
