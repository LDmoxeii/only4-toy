package com.only4.hashMap

class HashMap<K : Any, V> {
    private var table: Array<Node<K, V>?> = arrayOfNulls(16)
    private var _size = 0
    val size: Int get() = _size

    private data class Node<K, V>(
        val key: K,
        var value: V,
        var next: Node<K, V>? = null // 单向链表，移除冗余的 pre 字段
    )

    fun put(key: K, value: V): V? {
        val index = key.indexOf(table.size)
        var node = table[index]

        // 桶为空，直接插入新节点
        if (node == null) {
            table[index] = Node(key, value)
            _size++
            resizeIfNeeded()
            return null
        }

        // 遍历链表，查找 key 是否已存在
        while (true) {
            if (node!!.key == key) {
                val oldValue = node.value
                node.value = value
                return oldValue
            }

            // 链表中未找到 key，插入新节点到末尾
            if (node.next == null) {
                node.next = Node(key, value)
                _size++
                resizeIfNeeded()
                return null
            }

            node = node.next
        }
    }

    fun get(key: K): V? {
        var node = table[key.indexOf(table.size)]
        while (node != null) {
            if (node.key == key) return node.value
            node = node.next
        }
        return null
    }

    fun remove(key: K): V? {
        val index = key.indexOf(table.size)
        var node = table[index] ?: return null

        // 处理头节点匹配的情况
        if (node.key == key) {
            table[index] = node.next
            _size--
            return node.value
        }

        // 遍历链表，查找并移除节点
        var prev = node
        var current = node.next
        while (current != null) {
            if (current.key == key) {
                prev.next = current.next
                _size--
                return current.value
            }
            prev = current
            current = current.next
        }
        return null
    }

    private fun resizeIfNeeded() {
        if (_size < table.size * 0.75) return

        val newSize = table.size * 2
        val newTable = arrayOfNulls<Node<K, V>>(newSize)

        // 重新哈希所有元素到新表
        for (head in table) {
            var current = head
            while (current != null) {
                val next = current.next
                val newIndex = current.key.indexOf(newSize)

                // 头插法插入新表
                current.next = newTable[newIndex]
                newTable[newIndex] = current

                current = next
            }
        }

        table = newTable
        println("Resized to ${table.size}")
    }

    private fun <K> K.indexOf(tableSize: Int): Int {
        return hashCode() and (tableSize - 1)
    }
}
