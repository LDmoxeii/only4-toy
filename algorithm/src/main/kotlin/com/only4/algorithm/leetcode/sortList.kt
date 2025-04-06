package com.only4.algorithm.leetcode

fun sortList(head: ListNode?): ListNode? {
    head?.next ?: return head
    fun middleNode(node: ListNode): ListNode {
        node.next ?: return node
        var (slow, fast) = node to node as ListNode?
        while (fast?.next?.next != null) {
            slow = slow.next!!
            fast = fast.next?.next
        }
        return slow.next!!.apply { slow.next = null }
    }

    fun sortTowList(l1: ListNode, l2: ListNode): ListNode? {
        if (l1 === l2) return l1
        var dummy = ListNode(0) as ListNode?
        var curr = dummy
        var l1 = sortTowList(l1, middleNode(l1))
        var l2 = sortTowList(l2, middleNode(l2))
        while (l1 != null && l2 != null) {
            curr!!.next = when {
                l1.`val` < l2.`val` -> l1.apply { l1 = l1.next }
                else -> l2.apply { l2 = l2.next }
            }
            curr = curr.next
        }
        curr!!.next = l1 ?: l2
        return dummy!!.next
    }

    return sortTowList(head, middleNode(head))
}

fun main() {
    val list = ListNode(4)
    list.next = ListNode(2)
    list.next!!.next = ListNode(1)
    list.next!!.next!!.next = ListNode(3)
    println(sortList(list))
}
