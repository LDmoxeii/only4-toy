package com.only4.algorithm.leetcode

class Node(var `val`: Int) {
    var next: Node? = null
    var random: Node? = null
}

fun copyRandomList(node: Node?): Node? {
    node ?: return null
    var currHead = node
    while (currHead != null) {
        val copyNode = Node(`val` = currHead.`val`).apply {
            next = currHead.next
            random = currHead.random
        }
        val next = currHead.next
        currHead.next = copyNode
        currHead = next
    }
    currHead = node
    while (currHead != null) {
        currHead.next!!.random = currHead.random?.next
        currHead = currHead.next!!.next
    }

    val dummy = Node(0).apply { next = node.next }
    currHead = node
    while (currHead != null) {
        val copyHead = currHead.next
        currHead.next = copyHead?.next
        copyHead?.next = copyHead.next?.next

        currHead = currHead.next
    }

    return dummy.next
}

fun main() {
    val node5 = Node(1)
    val node4 = Node(10).apply { next = node5 }
    val node3 = Node(11).apply { next = node4 }
    val node2 = Node(13).apply { next = node3 }
    val node1 = Node(7).apply { next = node2 }

    node2.random = node1
    node3.random = node5
    node4.random = node3
    node5.random = node1

    copyRandomList(node1)
}
