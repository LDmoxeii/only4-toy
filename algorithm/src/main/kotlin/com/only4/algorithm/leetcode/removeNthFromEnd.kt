package com.only4.algorithm.leetcode

fun removeNthFromEnd(head: ListNode?, n: Int): ListNode? {
    ListNode(0).apply { next = head }.let { dummy ->
        var slow = dummy
        var fast = dummy as ListNode?

        repeat(n + 1) { fast = fast?.next }

        while (fast != null) {
            slow = slow.next!!
            fast = fast.next
        }

        slow.next = slow.next?.next
        return dummy.next
    }
}

fun main() {
    val head = ListNode(1).also {
        it.next = ListNode(2).also {
            it.next = ListNode(3).also {
                it.next = ListNode(4).also {
                    it.next = ListNode(5)
                }
            }
        }
    }
    println(removeNthFromEnd(head, 2))
}
