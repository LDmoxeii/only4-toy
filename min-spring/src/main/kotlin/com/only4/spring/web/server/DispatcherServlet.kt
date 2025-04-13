package com.only4.spring.web.server

import com.alibaba.fastjson2.JSONObject
import com.only4.spring.frame.annotation.Component
import com.only4.spring.frame.processor.BeanPostProcessor
import com.only4.spring.web.annotation.Controller
import com.only4.spring.web.annotation.Param
import com.only4.spring.web.annotation.RequestMapping
import com.only4.spring.web.handler.WebHandler
import com.only4.spring.web.model.ModelAndView
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class DispatcherServlet : HttpServlet(), BeanPostProcessor {
    companion object {
        private val PATTERN: Pattern = Pattern.compile("shengsheng\\{(.*?)}")
    }

    private val handlerMap = mutableMapOf<String, WebHandler>()


    override fun service(
        req: HttpServletRequest,
        resp: HttpServletResponse
    ) {
        val handler = findHandler(req)
        handler?.let {
            val controllerBean = handler.controllerBean
            val args: Array<Any?> = resolveArgs(req, handler.method)
            val result = handler.method.invoke(controllerBean, *args)
            when (handler.resultType) {
                WebHandler.ResultType.HTML -> {
                    resp.contentType = "text/html;charset=UTF-8"
                    resp.writer.write(result.toString())
                }

                WebHandler.ResultType.JSON -> {
                    resp.contentType = "application/json;charset=UTF-8"
                    resp.writer.write(JSONObject.toJSONString(result))
                }

                WebHandler.ResultType.LOCAL -> {
                    val mv = result as ModelAndView
                    val view = mv.view
                    val resourceAsStream = this::class.java.classLoader.getResourceAsStream(view)
                    resourceAsStream.run {
                        var html = String(this.readAllBytes())
                        html = renderTemplate(html, mv.context)
                        resp.contentType = "text/html;charset=UTF-8"
                        resp.writer.write(html)
                    }
                }
            }
        } ?: {
            resp.contentType = "text/html;charset=UTF-8"
            resp.writer.write("<h1>Error 你的请求没有对应的处理器!</h1> <br> ")
        }
    }

    private fun resolveArgs(req: HttpServletRequest, method: Method): Array<Any?> {
        val parameters = method.parameters
        val args = arrayOfNulls<Any?>(parameters.size)
        parameters.forEachIndexed { index, it ->
            val param = it.getAnnotation<Param>(Param::class.java)
            val value = param?.let { req.getParameter(param.value) } ?: req.getParameter(it.name)
            val parameterType = it.type
            args[index] = when {
                String::class.java.isAssignableFrom(parameterType) -> value
                Integer::class.java.isAssignableFrom(parameterType) -> value.toInt()
                else -> null
            }
        }
        return args
    }

    private fun renderTemplate(
        template: String,
        context: MutableMap<String, String>
    ): String {
        val matcher = PATTERN.matcher(template)
        val sb = StringBuilder()
        while (matcher.find()) {
            val key = matcher.group(1)
            val value = context.getOrDefault(key, "")
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun findHandler(req: HttpServletRequest) = handlerMap[req.requestURI]

    override fun afterInitializeBean(bean: Any, beanName: String): Any {
        if (!(bean::class.java.isAnnotationPresent(Controller::class.java))) return bean
        val classRm = bean::class.java.getAnnotation(RequestMapping::class.java)
        var classUrl = classRm?.let { classRm.value } ?: ""
        bean::class.java.declaredMethods
            .filter { it.isAnnotationPresent(RequestMapping::class.java) }
            .forEach {
                val methodRm = it.getAnnotation(RequestMapping::class.java)
                val key = classUrl.plus(methodRm.value)
                val webHandler = WebHandler(bean, it)
                handlerMap.put(key, webHandler)?. let { throw RuntimeException("controller定义重复: $key") }
            }
        return bean
    }
}




