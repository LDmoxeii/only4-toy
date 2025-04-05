package com.only4.schedule.service

import com.only4.schedule.entity.Job
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.LockSupport

class ScheduleService {
    private val trigger: Trigger = Trigger()
    private val executorService: ExecutorService = Executors.newFixedThreadPool(6)

    fun schedule(task: Runnable, delay: Long) {
        Job(
            task = task,
            startTime = System.currentTimeMillis() + delay,
            delay = delay
        ).also { trigger.queue.offer(it) }
        trigger.wakeUp()
    }

    inner class Trigger {
        val queue: PriorityBlockingQueue<Job> = PriorityBlockingQueue()

        private val thread: Thread = Thread {
            while (true) {
                while (queue.isEmpty()) LockSupport.park()
                var latelyJob = queue.element()
                if (latelyJob.startTime < System.currentTimeMillis()) {
                    latelyJob = queue.take()
                    executorService.execute { latelyJob.task.run() }
                    Job(
                        task = latelyJob.task,
                        startTime = latelyJob.startTime + latelyJob.delay,
                        delay = latelyJob.delay
                    ).also { queue.offer(it) }
                } else {
                    LockSupport.parkUntil(latelyJob.startTime)
                }
            }
        }

        init {
            thread.start()
            println("触发器已启动")
        }

        fun wakeUp() = LockSupport.unpark(thread)
    }
}

fun main() {
    val ofPattern = DateTimeFormatter.ofPattern("HH:mm:ss SSS")
    ScheduleService().apply {
        println("当前时间：${ofPattern.format(LocalDateTime.now())}")
        schedule({ println("${ofPattern.format(LocalDateTime.now())} 这是 100毫秒一次的") }, 100)
        Thread.sleep(1000)
        println("当前时间：${ofPattern.format(LocalDateTime.now())}")
        schedule({ println("${ofPattern.format(LocalDateTime.now())} 这是 200毫秒一次的") }, 200)
    }
}
