package com.only4.algorithm.leetcode

fun mergeKLists(lists: Array<ListNode?>): ListNode? {
    fun ListNode?.mergeWith(other: ListNode?): ListNode? {
        val dummy = ListNode(0)
        var curr = dummy
        var (l1, l2) = this to other
        while (l1 != null && l2 != null) {
            curr.next = when {
                l1.`val` < l2.`val` -> l1.apply { l1 = l1.next }
                else -> l2.apply { l2 = l2.next }
            }
            curr = curr.next!!
        }
        curr.next = l1 ?: l2
        return dummy.next
    }
    fun mergeRange(start: Int, end: Int): ListNode? = when {
        start > end -> null
        start == end -> lists[start]
        else -> {
            val mid = (start + end) / 2
            mergeRange(start, mid).mergeWith(mergeRange(mid + 1, end))
        }
    }

    return lists.takeUnless { it.isEmpty() }?.let {
        mergeRange(0, it.lastIndex)
    }
}

fun main() {
    val list1 = ListNode(1).apply {
        next = ListNode(4).apply {
            next = ListNode(5)
        }
    }
    val list2 = ListNode(1).apply {
        next = ListNode(3).apply {
            next = ListNode(4)
        }
    }
    val list3 = ListNode(2).apply {
        next = ListNode(6)
    }
    val merged = mergeKLists(arrayOf(list1, list2, list3))
}
