package com.only4.schedule.entity

data class Job(
    val task: Runnable,
    val startTime: Long,
    val delay: Long,
) : Comparable<Job> {
    override fun compareTo(other: Job): Int {
        return this.startTime.compareTo(other.startTime)
    }
}
