package com.only4.algorithm.leetcode


fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
    val graph = prerequisites.groupBy({ it[0] }, { it[1] })
        .withDefault { emptyList() }
    val states = IntArray(numCourses)

    fun traverse(course: Int): Boolean {
        states[course] = 1
        for (dependency in graph.getValue(course)) {
            when (states[dependency]) {
                1 -> return false
                2 -> continue
                else -> if (!traverse(dependency)) return false
            }
        }
        states[course] = 2
        return true
    }

    return (0 until numCourses).all { states[it] != 0 || traverse(it) }
}
