package com.only4.spring.web.handler

import com.only4.spring.web.annotation.ResponseBody
import com.only4.spring.web.model.ModelAndView
import java.lang.reflect.Method

class WebHandler(val controllerBean: Any, val method: Method) {
    val resultType: ResultType

    init {
        this.resultType = resolveResultType(controllerBean, method)
    }

    private fun resolveResultType(controllerBean: Any, method: Method): ResultType {
        return when {
            method.isAnnotationPresent(ResponseBody::class.java) -> ResultType.JSON
            method.returnType == ModelAndView::class.java -> ResultType.LOCAL
            else -> ResultType.HTML
        }
    }

    enum class ResultType { JSON, HTML, LOCAL }
}
