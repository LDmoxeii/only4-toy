package com.only4.algorithm.leetcode

class ListNode(var `val`: Int) {
    var next: ListNode? = null
}

fun mergeTwoLists(list1: ListNode?, list2: ListNode?): ListNode? {
    val dummy = ListNode(0)
    var head = dummy
    var l1 = list1
    var l2 = list2

    while (l1 != null && l2 != null) {
        head =
            (if (l1.`val` < l2.`val`) head.next = l1.also { l1 = it.next }
            else head.next = l2.also { l2 = it.next })
                .run { head.next!! }
    }
    head.next = l1 ?: l2

    return dummy.next
}

fun main() {
    val list1 = ListNode(1).also { it.next = ListNode(2).also { it.next = ListNode(4) } }
    val list2 = ListNode(1).also { it.next = ListNode(3).also { it.next = ListNode(4) } }
    println(mergeTwoLists(list1, list2))
}

