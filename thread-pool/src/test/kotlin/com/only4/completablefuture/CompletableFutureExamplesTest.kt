package com.only4.completablefuture

import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CompletableFuture API示例
 * 演示Java CompletableFuture常用方法
 */
class CompletableFutureExamplesTest {

    /**
     * 1.2.1 创建异步任务
     */
    @Test
    fun testCreateAsyncTask() {
        // runAsync - 无返回值
        val runAsyncFuture = CompletableFuture.runAsync {
            println("执行无返回值的异步任务")
            Thread.sleep(100)
        }
        // 等待任务完成
        runAsyncFuture.get()
        assertTrue(runAsyncFuture.isDone)

        // supplyAsync - 有返回值
        val supplyAsyncFuture = CompletableFuture.supplyAsync {
            println("执行有返回值的异步任务")
            Thread.sleep(100)
            "异步任务结果"
        }
        // 获取任务结果
        val result = supplyAsyncFuture.get()
        assertEquals("异步任务结果", result)
    }

    /**
     * 1.2.2 任务编排 - thenApply
     * 对结果进行转换
     */
    @Test
    fun testThenApply() {
        val future = CompletableFuture.supplyAsync {
            "原始结果"
        }.thenApply { result ->
            // 对结果进行转换
            "$result - 被转换"
        }

        val result = future.get()
        assertEquals("原始结果 - 被转换", result)
    }

    /**
     * 1.2.2 任务编排 - thenAccept
     * 消费结果，无返回
     */
    @Test
    fun testThenAccept() {
        var consumed = ""

        val future = CompletableFuture.supplyAsync {
            "需要被消费的结果"
        }.thenAccept { result ->
            // 消费结果，无返回
            consumed = result
        }

        // 等待执行完成
        future.get()
        assertEquals("需要被消费的结果", consumed)
    }

    /**
     * 1.2.2 任务编排 - thenCombine
     * 两个任务都完成后合并结果
     */
    @Test
    fun testThenCombine() {
        val future1 = CompletableFuture.supplyAsync {
            Thread.sleep(100)
            "Hello"
        }

        val future2 = CompletableFuture.supplyAsync {
            Thread.sleep(50)
            "World"
        }

        val combinedFuture = future1.thenCombine(future2) { result1, result2 ->
            "$result1 $result2"
        }

        val result = combinedFuture.get()
        assertEquals("Hello World", result)
    }

    /**
     * 1.2.2 任务编排 - thenCompose
     * 任务串联（类似flatMap）
     */
    @Test
    fun testThenCompose() {
        val future = CompletableFuture.supplyAsync {
            "第一个任务"
        }.thenCompose { result ->
            // 基于第一个任务的结果创建一个新的CompletableFuture
            CompletableFuture.supplyAsync {
                "$result -> 第二个任务"
            }
        }

        val result = future.get()
        assertEquals("第一个任务 -> 第二个任务", result)
    }

    /**
     * 1.2.3 并行与聚合 - allOf
     * 等待所有任务完成
     */
    @Test
    fun testAllOf() {
        val future1 = CompletableFuture.supplyAsync {
            Thread.sleep(100)
            "任务1结果"
        }

        val future2 = CompletableFuture.supplyAsync {
            Thread.sleep(150)
            "任务2结果"
        }

        val future3 = CompletableFuture.supplyAsync {
            Thread.sleep(50)
            "任务3结果"
        }

        // 等待所有任务完成
        val allFuture = CompletableFuture.allOf(future1, future2, future3)
        allFuture.get() // 等待所有任务完成，返回值为void

        // 此时所有任务都已完成，可以分别获取结果
        assertTrue(future1.isDone && future2.isDone && future3.isDone)
        assertEquals("任务1结果", future1.get())
        assertEquals("任务2结果", future2.get())
        assertEquals("任务3结果", future3.get())
    }

    /**
     * 1.2.3 并行与聚合 - anyOf
     * 任一任务完成即返回
     */
    @Test
    fun testAnyOf() {
        val future1 = CompletableFuture.supplyAsync {
            Thread.sleep(100)
            "任务1结果"
        }

        val future2 = CompletableFuture.supplyAsync {
            Thread.sleep(50) // 这个最快完成
            "任务2结果"
        }

        val future3 = CompletableFuture.supplyAsync {
            Thread.sleep(150)
            "任务3结果"
        }

        // 任一任务完成即返回
        val anyFuture = CompletableFuture.anyOf(future1, future2, future3)
        val result = anyFuture.get()

        // 由于任务2最快完成，所以应该返回任务2的结果
        assertEquals("任务2结果", result)
    }

    /**
     * 1.2.4 异常处理 - exceptionally
     * 处理异常并提供备选结果
     */
    @Test
    fun testExceptionally() {
        val future = CompletableFuture.supplyAsync<String> {
            if (true) { // 模拟异常
                throw RuntimeException("故意抛出异常")
            }
            "正常结果"
        }.exceptionally { ex ->
            // 发生异常时提供备选结果
            "异常备选结果: ${ex.message}"
        }

        val result = future.get()
        assertEquals("异常备选结果: java.lang.RuntimeException: 故意抛出异常", result)
    }

    /**
     * 1.2.4 异常处理 - whenComplete
     * 无论是否异常都会执行，但不改变结果或异常
     */
    @Test
    fun testWhenComplete() {
        var completionStatus = ""

        val future = CompletableFuture.supplyAsync<String> {
            if (true) { // 模拟异常
                throw RuntimeException("故意抛出异常")
            }
            "正常结果"
        }.whenComplete { result, ex ->
            if (ex != null) {
                completionStatus = "发生异常: ${ex.message}"
            } else {
                completionStatus = "正常完成: $result"
            }
        }

        try {
            future.get() // 这里会抛出异常
        } catch (e: ExecutionException) {
            // 由于whenComplete不会处理异常，所以这里会抛出原始异常
            assertTrue(e.cause is RuntimeException)
            assertEquals("故意抛出异常", e.cause?.message)
        }

        assertEquals("发生异常: java.lang.RuntimeException: 故意抛出异常", completionStatus)
    }

    /**
     * 1.2.4 异常处理 - handle
     * 处理结果或异常，并可以转换结果
     */
    @Test
    fun testHandle() {
        // 正常情况
        val normalFuture = CompletableFuture.supplyAsync {
            "正常结果"
        }.handle { result, ex ->
            if (ex != null) {
                "异常结果: ${ex.message}"
            } else {
                "处理后: $result"
            }
        }

        assertEquals("处理后: 正常结果", normalFuture.get())

        // 异常情况
        val exceptionalFuture = CompletableFuture.supplyAsync<String> {
            throw RuntimeException("故意抛出异常")
        }.handle { result, ex ->
            if (ex != null) {
                "异常结果: ${ex.message}"
            } else {
                "处理后: $result"
            }
        }

        assertEquals("异常结果: java.lang.RuntimeException: 故意抛出异常", exceptionalFuture.get())
    }

    /**
     * 补充: 超时控制
     */
    @Test
    fun testTimeout() {
        val future = CompletableFuture.supplyAsync {
            Thread.sleep(500) // 模拟耗时操作
            "操作完成"
        }

        // 尝试在超时内获取结果
        val result = future.orTimeout(1000, TimeUnit.MILLISECONDS).get()
        assertEquals("操作完成", result)

        // 超时情况下提供默认值
        val timeoutFuture = CompletableFuture.supplyAsync<String> {
            Thread.sleep(500) // 模拟耗时操作
            "操作完成"
        }.completeOnTimeout("默认结果", 100, TimeUnit.MILLISECONDS)

        val timeoutResult = timeoutFuture.get()
        assertEquals("默认结果", timeoutResult)
    }

    /**
     * 补充: 其他常用转换方法
     */
    @Test
    fun testOtherTransformations() {
        // thenApplyAsync - 异步转换
        val asyncTransform = CompletableFuture.supplyAsync {
            "原始值"
        }.thenApplyAsync {
            "$it - 异步转换"
        }

        assertEquals("原始值 - 异步转换", asyncTransform.get())

        // thenRun - 前一步完成后执行操作，无参数，无返回值
        var executed = false
        val runFuture = CompletableFuture.supplyAsync {
            "结果"
        }.thenRun {
            executed = true
        }

        runFuture.get()
        assertTrue(executed)

        // applyToEither - 两个任务任一完成，获取并转换其结果
        val future1 = CompletableFuture.supplyAsync {
            Thread.sleep(100)
            "任务1"
        }

        val future2 = CompletableFuture.supplyAsync {
            Thread.sleep(50)
            "任务2"
        }

        val eitherFuture = future1.applyToEither(future2) {
            "最快的是: $it"
        }

        assertEquals("最快的是: 任务2", eitherFuture.get())
    }
}
