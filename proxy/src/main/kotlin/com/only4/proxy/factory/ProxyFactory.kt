package com.only4.proxy.factory

import com.only4.proxy.handler.SimpleHandler
import com.only4.proxy.intf.SimpleInterface
import com.only4.proxy.utils.Compiler
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

object ProxyFactory {
    val count = AtomicInteger()

    fun createKotlinFile(className: String, handler: SimpleHandler): File {
        val func1Body = handler.functionBody("func1")
        val func2Body = handler.functionBody("func2")
        val func3Body = handler.functionBody("func3")
        val context = """
        package com.only4.proxy.generated;

        import com.only4.proxy.intf.SimpleInterface;

        public class $className implements SimpleInterface {
            SimpleInterface simpleInterface;

            @Override
            public void func1() {
                $func1Body
            }

            @Override
            public void func2() {
                $func2Body
            }

            @Override
            public void func3() {
                $func3Body
            }
        }
        """.trimIndent()
        val file = File("./only4-toy/proxy/src/main/java/com/only4/proxy/generated/${className}.java")
        Files.writeString(file.toPath(), context)
        return file
    }

    private fun getClassName(): String {
        return "SimpleInterface\$proxy${count.getAndIncrement()}"
    }

    private fun newInstance(className: String, handler: SimpleHandler): SimpleInterface {
        val clazz = ProxyFactory::class.java.classLoader.loadClass(className)
        val constructor = clazz.getDeclaredConstructor()
        val proxy = constructor.newInstance() as SimpleInterface
        handler.setProxy(proxy)
        return proxy
    }

    fun createProxy(handler: SimpleHandler): SimpleInterface {
        val className = getClassName()
        val file = createKotlinFile(className, handler)
        Compiler.compile(file)
        return newInstance("com.only4.proxy.generated.${className}", handler)
    }
}

fun main() {
    val handler1 = object : SimpleHandler {
        override fun functionBody(methodName: String): String {
            return """
            String methodName = "$methodName";
                            System.out.println(methodName);
                            System.out.println(methodName.length());
            """.trimIndent()
        }
    }
    val instance1 = ProxyFactory.createProxy(handler1)
    instance1.func1()
    instance1.func2()
    instance1.func3()

    println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------")

    val instance2 = ProxyFactory.createProxy(LogHandler(instance1))
    instance2.func1()
    instance2.func2()
    instance2.func3()
}


class LogHandler(
    private val advice: SimpleInterface
) : SimpleHandler {
    override fun functionBody(methodName: String): String {
        val context = """
            System.out.println("before");
                            simpleInterface.$methodName();
                            System.out.println("after");
        """.trimIndent()
        return context
    }

    override fun setProxy(proxy: SimpleInterface) {
        val aClass: Class<out SimpleInterface> = proxy::class.java
        val field = aClass.getDeclaredField("simpleInterface")
        field.setAccessible(true)
        field.set(proxy, advice)
    }
}
