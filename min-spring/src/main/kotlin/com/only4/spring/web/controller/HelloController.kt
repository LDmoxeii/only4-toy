package com.only4.spring.web.controller

import com.only4.spring.frame.annotation.Component
import com.only4.spring.web.annotation.Controller
import com.only4.spring.web.annotation.Param
import com.only4.spring.web.annotation.RequestMapping
import com.only4.spring.web.annotation.ResponseBody
import com.only4.spring.web.entity.User
import com.only4.spring.web.model.ModelAndView

@Controller
@Component
@RequestMapping("/hello")
class HelloController {
    @RequestMapping("/a")
    fun hello(@Param("name") name: String?, @Param("age") age: Int?): String {
        return String.format("<h1>hello world</h1><br> name: %s  age:%s", name, age)
    }

    @RequestMapping("/json")
    @ResponseBody
    fun json(@Param("name") name: String?, @Param("age") age: Int?): User {
        val user: User = User()
        user.age = age
        user.name = name
        return user
    }

    @RequestMapping("/html")
    fun html(@Param("name") name: String?, @Param("age") age: Int?): ModelAndView {
        val modelAndView: ModelAndView = ModelAndView("index.html")
        modelAndView.context.put("name", name!!)
        modelAndView.context.put("age", age.toString())
        return modelAndView
    }

    @RequestMapping("/aa")
    fun hellao(): String {
        return "sadf"
    }
}

fun main() {
    val helloController = HelloController()

    println(HelloController::hello.invoke(helloController, "only4", 20))
    println(helloController::hello.invoke("only4", 20))
    println(
        HelloController::class.java.getMethod("hello", String::class.java, Integer::class.java)
            .invoke(helloController, "only4", 20)
    )
}
