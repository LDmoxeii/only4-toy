package com.only4.machine.state

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class StatemachineApplication

fun main(args: Array<String>) {
    SpringApplication.run(StatemachineApplication::class.java, *args)
}


