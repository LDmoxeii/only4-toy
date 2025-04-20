package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode
import java.util.*

fun mergeKLists(lists: Array<ListNode?>): ListNode? {
    if (lists.isEmpty()) return null

    ListNode(-1).let { dummy ->
        var p = dummy
        val pq = PriorityQueue<ListNode>(lists.size, compareBy<ListNode> { it.`val` })

        lists.filterNotNull()
            .forEach { pq.add(it) }

        while (pq.isNotEmpty()) {
            val node = pq.poll()
            p.next = node
            node.next?.let { pq.add(it) }
            p = p.next!!
        }
        return dummy.next
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
