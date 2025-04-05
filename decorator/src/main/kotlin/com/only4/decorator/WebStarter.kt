package com.only4.decorator

import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
open class WebStarter

fun main(args: Array<String>) {
    org.springframework.boot.runApplication<WebStarter>(*args)
}
