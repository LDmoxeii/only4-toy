package com.only4.spring.web.server

import com.only4.spring.frame.annotation.Autowired
import com.only4.spring.frame.annotation.Component
import com.only4.spring.frame.annotation.PostConstruct
import org.apache.catalina.startup.Tomcat
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.util.logging.LogManager

@Component
class TomcatServer {

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    @PostConstruct
    fun start() {
        LogManager.getLogManager().reset()
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        val port = 8080
        val tomcat = Tomcat()
        tomcat.setPort(port)
        tomcat.connector

        val contextPath = ""
        val docBase = File(".").absolutePath
        val context = tomcat.addContext(contextPath, docBase)

        tomcat.addServlet(contextPath, "dispatcherServlet", dispatcherServlet)
        context.addServletMappingDecoded("/*", "dispatcherServlet")
        tomcat.start()
        println("Tomcat server started on port $port")
    }

}


