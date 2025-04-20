package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun partition(s: String): List<List<String>> {
    var result = mutableListOf<List<String>>()
    var path = mutableListOf<String>()
    fun dfs(start: Int, end: Int) {
        if (end == s.length) result.add(path.toList()).run { return }

        if (end < s.lastIndex) dfs(start, end + 1)

        val subString = s.substring(start, end + 1)
        if (subString == subString.reversed()) {
            path.add(subString)
            dfs(end + 1, end + 1)
            path.removeLast()
        }
    }
    dfs(0, 0)
    return result
}

fun partition(head: ListNode?, x: Int): ListNode? {
    head ?: return null
    val dummy1 = ListNode(-10)
    val dummy2 = ListNode(-20)
    var head = head
    var (head1, head2) = dummy1 to dummy2

    while (head != null) {
        when {
            head.`val` < x -> {
                head1.next = head.apply { head = next }
                head1 = head1.next!!
            }

            else -> {
                head2.next = head.apply { head = next }
                head2 = head2.next!!
            }
        }
    }
    head1.next = dummy2.next
    head2.next = null
    return dummy1.next
}

fun main() {
    println(partition("aab"))
    // [1,4,3,2,5,2]
    var head = ListNode(1).apply {
        next = ListNode(4).apply {
            next = ListNode(3).apply {
                next = ListNode(2).apply {
                    next = ListNode(5).apply {
                        next = ListNode(2)
                    }
                }
            }
        }
    }
    println(partition(head, 3))
}
