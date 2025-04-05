//package com.only4.hashMap
//
//class MyHashMap<K, V> {
//    private var table = arrayOfNulls<Node<K, V>>(16)
//
//    private var size = 0
//
//    fun put(key: K, value: V): V? {
//        val keyIndex = indexOf(key!!)
//        var head = table[keyIndex]
//        if (head == null) {
//            table[keyIndex] = Node<K, V>(key, value)
//            size++
//            resizeIfNecessary()
//            return null
//        }
//        while (true) {
//            if (head.key == key) {
//                val oldValue: V = head.value
//                head.value = value
//                return oldValue
//            }
//            if (head.next == null) {
//                head.next = Node<K, V>(key, value)
//                size++
//                resizeIfNecessary()
//                return null
//            }
//            head = head.next
//        }
//    }
//
//    fun get(key: K?): V? {
//        val keyIndex = indexOf(key!!)
//        var head: Node<K?, V?>? = table[keyIndex]
//        while (head != null) {
//            if (head.key == key) {
//                return head.value
//            }
//            head = head.next
//        }
//        return null
//    }
//
//    fun remove(key: K?): V? {
//        val keyIndex = indexOf(key!!)
//        val head: Node<K?, V?>? = table[keyIndex]
//        if (head == null) {
//            return null
//        }
//        if (head.key == key) {
//            table[keyIndex] = head.next
//            size--
//            return head.value
//        }
//        var pre: Node<K?, V?>? = head
//        var current: Node<K?, V?>? = head.next
//        while (current != null) {
//            if (current.key == key) {
//                pre.next = current.next
//                size--
//                return current.value
//            }
//            pre = pre.next
//            current = current.next
//        }
//        return null
//    }
//
//
//    private fun resizeIfNecessary() {
//        if (this.size < table.size * 0.75) {
//            return
//        }
//        val newTable: Array<Node<K?, V?>?> = kotlin.arrayOfNulls<Node<*, *>>(this.table.size * 2)
//        for (head in this.table) {
//            if (head == null) {
//                continue
//            }
//            var current: Node<K?, V?>? = head
//            while (current != null) {
//                val newIndex = current.key.hashCode() and (newTable.size - 1)
//                if (newTable[newIndex] == null) {
//                    newTable[newIndex] = current
//                    val next: Node<K?, V?>? = current.next
//                    current.next = null
//                    current = next
//                } else {
//                    val next: Node<K?, V?>? = current.next
//                    current.next = newTable[newIndex]
//                    newTable[newIndex] = current
//                    current = next
//                }
//            }
//        }
//        this.table = newTable
//        println("扩容了，扩容到" + this.table.size)
//    }
//
//    fun size(): Int {
//        return size
//    }
//
//    private fun indexOf(key: Any): Int {
//        return key.hashCode() and (table.size - 1)
//    }
//
//    internal inner class Node<K, V>(var key: K, var value: V) {
//        var pre: Node<K, V>? = null
//        var next: Node<K, V>? = null
//    }
//}
