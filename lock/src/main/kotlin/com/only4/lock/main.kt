package com.only4.lock

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport

class Lock {
    private val flag = AtomicBoolean(false)
    private lateinit var owner: Thread

    var head = AtomicReference<Node>(Node(thread = null))
    var tail = AtomicReference<Node>(head.get())

    fun lock() {
        if (flag.compareAndSet(false, true)) {
            println(Thread.currentThread().name + "获得锁")
            owner = Thread.currentThread()
            return
        }
        val current = Node(thread = Thread.currentThread())
        while (true) {
            val currentTail = tail.get()
            if (tail.compareAndSet(currentTail, current)) {
                println(Thread.currentThread().name + "等待锁")
                current.prev = currentTail
                currentTail.next = current
                break
            }
        }
        while (true) {
            if (current.prev == head.get() && flag.compareAndSet(false, true)) {
                owner = Thread.currentThread()
                head.set(current)
                current.prev?.next = null
                current.prev = null
                println(Thread.currentThread().name + "获得锁")
                return
            }
            LockSupport.park()
        }
    }

    fun unlock() {
        if (Thread.currentThread() != owner) throw IllegalMonitorStateException()
        flag.set(false)
        val headNode = head.get()
        val next = headNode.next
        if (next != null) {
            LockSupport.unpark(next.thread)
        }
    }

    data class Node(
        var prev: Node? = null,
        var next: Node? = null,
        val thread: Thread?
    )
}

fun main() {
    var count = 1000
    val threads = mutableListOf<Thread>()
    val lock = Lock()
    for (i in 1..100) {
        threads.add(Thread {
            lock.lock()
            for (j in 1..10) {
                Thread.sleep(2)
                count--
            }
            lock.unlock()
        })
    }

    threads.forEach {
        it.start()
    }

    threads.forEach {
        it.join()
    }
    println("count: $count")
}
