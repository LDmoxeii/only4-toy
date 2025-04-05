package com.only4.pool

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class ThreadPool(
    private val corePoolSize: Int = 2,
    private val maxPoolSize: Int = 95,
    private val keepAliveTime: Long = 60,
    private val unit: TimeUnit = TimeUnit.SECONDS,
    private val blockingQueue: BlockingQueue<Runnable> = ArrayBlockingQueue<Runnable>(5),
    private val rejectHandler: RejectHandler = object : RejectHandler {
        override fun reject(command: Runnable, pool: ThreadPool) {
            pool.blockingQueue.poll()
            pool.execute { command.run() }
        }
    }
) {


    val coreList = mutableListOf<Thread>()
    val supportList = mutableListOf<Thread>()

    fun execute(command: Runnable) {
        if (coreList.size < corePoolSize) {
            val thread = CoreThread()
            coreList.add(thread)
            thread.start()
        }

        if (blockingQueue.offer(command)) return

        if (coreList.size + supportList.size < maxPoolSize) {
            val thread = SupportThread()
            supportList.add(thread)
            thread.start()

        } else if (!(blockingQueue.offer(command)))
            rejectHandler.reject(command, this)
    }

    inner class CoreThread(
    ) : Thread() {
        override fun run() {
            while (true) {
                try {
                    val command = blockingQueue.take()
                    command.run()
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

        }

    }

    inner class SupportThread(
    ) : Thread() {
        override fun run() {
            while (true) {
                try {
                    val command = blockingQueue.poll(keepAliveTime, unit)
                    if (command == null)
                        break
                    command.run()
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
            println("辅助线程${currentThread().name}结束了！")
        }
    }
}

interface RejectHandler {
    fun reject(command: Runnable, pool: ThreadPool)
}

fun main() {
    val threadPool = ThreadPool(2, 6, 1, TimeUnit.SECONDS, ArrayBlockingQueue(2))
    for (i in 1..10) {
        threadPool.execute {
            Thread.sleep(1000)
            println("${Thread.currentThread().name} $i")
        }
    }

    println("主线程没有被阻塞")
}
