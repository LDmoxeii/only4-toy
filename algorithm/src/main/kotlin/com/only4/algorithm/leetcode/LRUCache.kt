package com.only4.algorithm.leetcode

class LRUCache(val capacity: Int) {

    val sentinel = Node().apply {
        next = this
        prev = this
    }
    val cache = mutableMapOf<Int, Node>()


    inner class Node(val key: Int = 0, var `val`: Int = 0) {
        var prev: Node = this
        var next: Node = this

        fun remove() {
            prev.next = next
            next.prev = prev
        }

        fun moveToTop() {
            next = sentinel.next
            prev = sentinel

            sentinel.next.prev = this
            sentinel.next = this
        }
    }

    fun get(key: Int): Int = getNode(key)?.`val` ?: - 1

    private fun getNode(key: Int): Node? {
        val node = cache[key] ?: return null
        node.remove()
        node.moveToTop()
        return node
    }

    fun put(key: Int, value: Int) {
        getNode(key)?.let {
            it.`val` = value
            return
        }
        Node(key, value).also { newNode ->
            cache[key] = newNode
            newNode.moveToTop()

            if (cache.size > capacity) {
                sentinel.prev.also { lastNode ->
                    lastNode.remove()
                    cache.remove(lastNode.key)
                }
            }
        }
    }
}
