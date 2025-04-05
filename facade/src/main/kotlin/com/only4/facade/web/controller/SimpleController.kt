package com.only4.facade.web.controller

import cn.hutool.core.io.resource.ResourceUtil
import com.only4.facade.spi.ResponseSPI
import org.springframework.web.bind.annotation.*
import java.io.File
import java.net.URLClassLoader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/simple")
class SimpleController {

    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")!!

    lateinit var spi: ResponseSPI

    @PostMapping("/time")
    fun getTime(): String {
        if (::spi.isInitialized) {
            spi.before()
        }
        return LocalDateTime.now().format(dateTimeFormatter)
    }

    @GetMapping("/loadSPI/{path}")
    fun loadSPI(@PathVariable("path") path: String) {
        try {
            val url = ResourceUtil.getResourceObj(path).url
            val file = File(url.toURI())
            URLClassLoader(arrayOf(file.toPath().toUri().toURL())).use {
                val asStream = it.getResourceAsStream("INF-META/only4.spi")?: throw RuntimeException("找不到spi文件")
                val fullClassName = String(asStream.readAllBytes())
                val clazz = it.loadClass(fullClassName.trim())
                spi = clazz.getDeclaredConstructor().newInstance() as ResponseSPI
            }
        } catch (e: Exception) {
            throw RuntimeException("加载spi失败", e)
        }
    }
}
