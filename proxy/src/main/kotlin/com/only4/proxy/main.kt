package com.only4.proxy

import java.sql.DriverManager
import java.sql.Statement

fun main() {
    // 1. 创建内存数据库连接
    val connection = DriverManager.getConnection(
        "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "sa",
        ""
    ).also {
        println("成功连接到H2内存数据库")
    }

    // 2. 创建user表
    connection.createStatement().use { stmt ->
        stmt.execute("""
            CREATE TABLE users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                age INT
            )
        """.trimIndent())
        println("成功创建users表")
    }

    // 3. 插入测试数据
    connection.prepareStatement(
        "INSERT INTO users (name, age) VALUES (?, ?)",
        Statement.RETURN_GENERATED_KEYS
    ).use { pstmt ->
        // 插入第一条数据
        pstmt.setString(1, "张三")
        pstmt.setInt(2, 25)
        pstmt.executeUpdate()

        // 插入第二条数据
        pstmt.setString(1, "李四")
        pstmt.setInt(2, 30)
        pstmt.executeUpdate()

        println("成功插入2条用户数据")
    }

    // 4. 查询数据
    connection.createStatement().use { stmt ->
        val rs = stmt.executeQuery("SELECT id, name, age FROM users")
        println("\n当前用户列表:")
        println("ID\t姓名\t年龄")
        println("----------------")
        while (rs.next()) {
            val id = rs.getInt("id")
            val name = rs.getString("name")
            val age = rs.getInt("age")
            println("$id\t$name\t$age")
        }
    }

    // 5. 关闭连接
    connection.close()
    println("\n数据库连接已关闭")
}
