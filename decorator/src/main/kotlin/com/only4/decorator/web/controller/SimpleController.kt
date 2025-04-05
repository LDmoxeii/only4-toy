package com.only4.decorator.web.controller

import com.only4.decorator.annotation.TimestampRequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/simple")
class SimpleController {

    @PostMapping("/hello")
    fun hello(@TimestampRequestBody json: Map<String, Any>): Map<String, Any> {
        return json
    }
}
