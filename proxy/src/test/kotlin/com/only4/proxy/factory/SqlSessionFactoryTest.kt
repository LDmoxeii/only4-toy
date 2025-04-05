import com.only4.proxy.entity.User
import com.only4.proxy.factory.SqlSessionFactory
import com.only4.proxy.mapper.UserMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqlSessionFactoryTest {
    private lateinit var connection: Connection

    @BeforeAll
    fun setUp() {
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        ).apply {
            createStatement().use { stmt ->
                stmt.execute("""
                CREATE TABLE users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    age INT
                )
                """.trimIndent())
            }

            prepareStatement("INSERT INTO users (name, age) VALUES (?, ?)").use { pstmt ->
                listOf(
                    "张三" to 25,
                    "李四" to 30
                ).forEach { (name, age) ->
                    pstmt.setString(1, name)
                    pstmt.setInt(2, age)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
            }
        }
        connection.close()
    }

    @Test
    fun `should return inserted data when query by id`() {
        DriverManager.getConnection(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        ).use { conn ->
            val user = conn.prepareStatement("SELECT id, name, age FROM users WHERE id = ?").use { stmt ->
                stmt.setInt(1, 1)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        User(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            age = rs.getInt("age")
                        )
                    } else null
                }
            }
            assertEquals(User(id = 1, name = "张三", age = 25), user)
        }
    }

    @Test
    fun `should work correctly with proxy mapper`() {
        val mapper = SqlSessionFactory.getMapper<UserMapper>()

        with(mapper) {
            assertEquals(User(1, "张三", 25), getUserById(1))
            assertEquals(User(2, "李四", 30), getUserById(2))
            assertEquals(User(1, "张三", 25), getUserByName("张三"))
            assertEquals(User(2, "李四", 30), getUserByName("李四"))
            assertEquals(User(1, "张三", 25), getUserByNameAndAge("张三", 25))
            assertEquals(User(2, "李四", 30), getUserByNameAndAge("李四", 30))
            assertNull(getUserByNameAndAge("张三", 30))
        }
    }
}
