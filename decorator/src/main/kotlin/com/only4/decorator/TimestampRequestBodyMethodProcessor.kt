package com.only4.decorator

import com.only4.decorator.annotation.TimestampRequestBody
import org.springframework.context.ApplicationContext
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor

class TimestampRequestBodyMethodProcessor(
    applicationContext: ApplicationContext,
    private val delegate: HandlerMethodArgumentResolver = lazyDelegate(applicationContext)
) : HandlerMethodArgumentResolver by delegate {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation<TimestampRequestBody?>(TimestampRequestBody::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        var result = delegate.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
        if (result !is Map<*, *>) {
            return result
        }
        return result.toMutableMap().apply {
            this["timestamp"] = System.currentTimeMillis()
        }
    }

    companion object {
        private fun lazyDelegate(applicationContext: ApplicationContext): HandlerMethodArgumentResolver {
            val adapter = applicationContext.getBean(RequestMappingHandlerAdapter::class.java)
            return adapter.argumentResolvers!!.filterIsInstance<RequestResponseBodyMethodProcessor>().first()
        }
    }
}
