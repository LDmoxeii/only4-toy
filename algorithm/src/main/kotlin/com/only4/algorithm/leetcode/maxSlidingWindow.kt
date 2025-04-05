package com.only4.algorithm.leetcode

import java.util.*

fun maxSlidingWindow(numbers: IntArray, windowSize: Int): IntArray {
    val maxValues = IntArray(numbers.size - windowSize + 1)
    val maxHeap = PriorityQueue<IndexedValue> { (_, value1), (_, value2) ->
        value2.compareTo(value1) // 降序排列，最大值在堆顶
    }.apply {
        // 初始化第一个窗口
        repeat(windowSize) { index ->
            this.offer(IndexedValue(index, numbers[index]))
        }
    }

    var resultIndex = 0
    for (windowEnd in windowSize - 1 until numbers.size) {
        // 添加当前元素到堆中
        maxHeap.offer(IndexedValue(windowEnd, numbers[windowEnd]))

        // 移除不在当前窗口范围内的最大值
        removeOutOfWindowElements(maxHeap, windowEnd - windowSize + 1)

        // 当前窗口的最大值就是堆顶元素
        maxValues[resultIndex++] = maxHeap.peek().value
    }

    return maxValues
}

/**
 * 移除堆顶不在当前窗口范围内的元素
 * @param windowStart 当前窗口的起始索引
 */
private fun removeOutOfWindowElements(
    heap: PriorityQueue<IndexedValue>,
    windowStart: Int
) {
    while (heap.peek().index < windowStart) {
        heap.poll()
    }
}

/**
 * 封装索引和值的配对，比Pair<Int, Int>更语义化
 */
private data class IndexedValue(val index: Int, val value: Int)

fun main() {
    val result = maxSlidingWindow(intArrayOf(1, 3, -1, -3, 5, 3, 6, 7), 3)
    println(result.joinToString())
}
