package com.only4.facade

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class WebStarter

fun main(args: Array<String>) {
    runApplication<WebStarter>(*args)
}
