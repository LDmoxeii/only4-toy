package com.only4.algorithm.leetcode

//fun threeSum(nums: IntArray): List<List<Int>> {
//    val ints = nums.sorted()
//    val result = mutableListOf<List<Int>>()
//    ints.forEachIndexed { index, it ->
//        var (left, right) = index + 1 to nums.lastIndex
//        while (left < right) {
//            val key = ints[left] + ints[right]
//            if (-it == key) {
//                val element = listOf<Int>(it, ints[left], ints[right])
//                if (!(result.contains(element))) result.add(element)
//            }
//            do right-- while (right - 1 > 0 && ints[right] == ints[right - 1])
//            do left++ while (left + 1 < ints.size && ints[left] == ints[left + 1])
//        }
//    }
//    return result
//}

fun threeSum(nums: IntArray): List<List<Int>> {
    nums.sort()
    val triplets = mutableListOf<List<Int>>()

    for (index in 0..nums.lastIndex - 2) {
        val current = nums[index]
        var (left, right) = index + 1 to nums.lastIndex

        // 跳过重复的当前数字
        if (index > 0 && current == nums[index - 1]) continue

        // 提前终止条件优化
        if (-current > nums[right - 1] + nums[right]) continue  // 剩余最大和仍不足
        if (-current < nums[left] + nums[left + 1]) continue   // 剩余最小和已超出

        while (left < right) {
            val twoSum = nums[left] + nums[right]

            when {
                -current == twoSum -> {
                    triplets.add(listOf(current, nums[left], nums[right]))
                    // 跳过重复的左值和右值
                    do left++ while (left < right && nums[left] == nums[left - 1])
                    do right-- while (left < right && nums[right] == nums[right + 1])
                }

                -current < twoSum -> right--
                -current > twoSum -> left++
            }
        }
    }
    return triplets
}

fun main() {
    val result = threeSum(intArrayOf(-1, 0, 1, 2, -1, -1, -4))
    val case1 = threeSum(intArrayOf(-2, 0, 1, 1, 2))
    val case2 = threeSum(intArrayOf(0, 0, 0))
    result.forEach {
        println(it)
    }
}
