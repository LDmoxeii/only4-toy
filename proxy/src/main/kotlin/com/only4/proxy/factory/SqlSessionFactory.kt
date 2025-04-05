package com.only4.proxy.factory

import com.only4.proxy.annotation.Param
import com.only4.proxy.annotation.Table
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.DriverManager
import java.sql.ResultSet

object SqlSessionFactory {
    private const val JDBC_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
    private const val DB_USER = "sa"
    private const val PASSWORD = ""

    inline fun <reified T : Any> getMapper(): T = getMapper(T::class.java)

    fun <T> getMapper(clazz: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            clazz.classLoader,
            arrayOf(clazz),
            MapperInvocationHandler(JDBC_URL, DB_USER, PASSWORD)
        ) as T
    }
}

class MapperInvocationHandler(
    private val jdbcUrl: String,
    private val dbUser: String,
    private val password: String
) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
        return when {
            method.name.startsWith("get") -> invokeSelect(method, args)
            else -> null
        }
    }

    private fun invokeSelect(method: Method, args: Array<out Any?>?): Any? {
        val sql = createSelectSql(method)

        return DriverManager.getConnection(jdbcUrl, dbUser, password).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                args?.forEachIndexed { index, arg ->
                    when (arg) {
                        is Int -> statement.setInt(index + 1, arg)
                        is String -> statement.setString(index + 1, arg)
                        else -> throw IllegalArgumentException("Unsupported parameter type: ${arg?.javaClass?.simpleName}")
                    }
                }
                statement.executeQuery().use { rs ->
                    if (rs.next()) parseResult(rs, method.returnType) else null
                }
            }
        }
    }

    private fun parseResult(rs: ResultSet, returnType: Class<*>): Any {
        val constructor = returnType.getDeclaredConstructor()
        val result = constructor.newInstance()

        returnType.declaredFields.forEach { field ->
            field.isAccessible = true
            when (field.type) {
                Int::class.java -> field.setInt(result, rs.getInt(field.name))
                String::class.java -> field.set(result, rs.getString(field.name))
                // 可以添加更多类型支持
            }
        }
        return result
    }

    private fun createSelectSql(method: Method): String {
        val selectCols = getSelectColumns(method.returnType)
        val tableName = getTableName(method.returnType)
        val whereClause = getWhereClause(method)
        return "SELECT $selectCols FROM $tableName WHERE $whereClause"
    }

    private fun getWhereClause(method: Method): String {
        return method.parameters.joinToString(" AND ") { param ->
            val column = param.getAnnotation(Param::class.java)?.value ?: param.name
            "$column = ?"
        }
    }

    private fun getTableName(returnType: Class<*>): String {
        return returnType.getAnnotation(Table::class.java)?.tableName
            ?: throw IllegalStateException("返回值无法确定查询表: ${returnType.simpleName}")
    }

    private fun getSelectColumns(returnType: Class<*>): String {
        return returnType.declaredFields.joinToString(", ") { it.name }
    }
}
