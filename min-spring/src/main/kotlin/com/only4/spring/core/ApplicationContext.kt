package com.only4.spring.core

import com.only4.spring.annotation.Component
import com.only4.spring.processor.BeanPostProcessor
import java.io.File

class ApplicationContext(packageName: String) {
    private val beanDefinitionMap = mutableMapOf<String, BeanDefinition>()
    private val ioc = mutableMapOf<String, Any>()
    private val loadingIoc = mutableMapOf<String, Any>()
    private val postProcessors = mutableListOf<BeanPostProcessor>()

    init {
        scanPackage(packageName)
            .filter(::isComponentPresent)
            .forEach(::registerBeanDefinition)

        initBeanPostProcessor()

        beanDefinitionMap.values.forEach(::createBean)
    }

    private fun isComponentPresent(type: Class<*>) = type.isAnnotationPresent(Component::class.java)

    private fun registerBeanDefinition(type: Class<*>) {
        BeanDefinition(type).also { definition ->
            check(definition.name !in beanDefinitionMap) { "Duplicate bean name: ${definition.name}" }
            beanDefinitionMap[definition.name] = definition
        }
    }

    private fun initBeanPostProcessor() {
        postProcessors += beanDefinitionMap.values
            .filter { BeanPostProcessor::class.java.isAssignableFrom(it.beanType) }
            .map(::createBean)
            .map { it as BeanPostProcessor }
    }

    private fun createBean(beanDefinition: BeanDefinition): Any {
        val name = beanDefinition.name
        ioc[name]?.let { return it }
        loadingIoc[name]?.let { return it }
        return doCreateBean(beanDefinition)
    }

    private fun doCreateBean(definition: BeanDefinition): Any {
        return definition.constructor.newInstance()
            .also { bean ->
                loadingIoc[definition.name] = bean
                injectDependencies(bean, definition)
            }
            .let { bean -> initializeBean(bean, definition) }
            .also { initializedBean ->
                loadingIoc.remove(definition.name)
                ioc[definition.name] = initializedBean
            }
    }

    private fun injectDependencies(bean: Any, definition: BeanDefinition) {
        definition.autowiredFields.forEach { field ->
            field.isAccessible = true
            field.set(bean, getBean(field.type))
        }
    }

    private fun initializeBean(bean: Any, definition: BeanDefinition): Any {
        return bean
            .apply { postProcessors.fold(this) { acc, processor ->
                processor.beforeInitializeBean(acc, definition.name)
            }}
            .apply { definition.postConstructMethod?.invoke(this) }
            .apply { postProcessors.fold(this) { acc, processor ->
                processor.afterInitializeBean(acc, definition.name)
            }}
    }

    private fun scanPackage(packageName: String): List<Class<*>> {
        val packagePath = packageName.replace(".", File.separator)
        val resource = javaClass.classLoader.getResource(packagePath) ?: return emptyList()

        return File(resource.toURI()).walk()
            .filter { it.isFile && it.name.endsWith(".class") }
            .mapNotNull { file ->
                file.toRelativeClassName(packageName, packagePath)
                    .takeIf(String::isNotEmpty)
                    ?.let{ Class.forName(it) }
            }
            .toList()
    }

    private fun File.toRelativeClassName(packageName: String, packagePath: String): String {
        val relativePath = this.path
            .substringAfter(packagePath)
            .drop(1) // Remove leading slash
            .replace(File.separator, ".")
            .removeSuffix(".class")
        return "$packageName.$relativePath"
    }

    fun getBean(name: String): Any {
        require(name.isNotBlank()) { "Bean name cannot be blank" }
        return ioc[name] ?: beanDefinitionMap[name]?.let(::createBean)
        ?: throw NoSuchBeanException("No bean named '$name' found")
    }

    fun <T> getBean(beanType: Class<T>): T {
        val candidates = beanDefinitionMap.values.filter { beanType.isAssignableFrom(it.beanType) }
        return when {
            candidates.isEmpty() -> throw NoSuchBeanException("No bean of type ${beanType.name} found")
            candidates.size > 1 -> throw NoUniqueBeanException("Multiple beans of type ${beanType.name} found")
            else -> getBean(candidates.single().name) as T
        }
    }

    fun <T> getBeans(beanType: Class<T>) = beanDefinitionMap.values
        .filter { beanType.isAssignableFrom(it.beanType) }
        .map { it.name }
        .map(::getBean)
        .map { it as T }
}

// Custom exceptions
class NoSuchBeanException(message: String) : RuntimeException(message)
class NoUniqueBeanException(message: String) : RuntimeException(message)
