package com.only4.algorithm.extra

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * SortingMultipleTreeImpl的单元测试类
 */
class SortingMultipleTreeImplTest {

    // 测试使用String作为键，String作为数据的树
    private lateinit var tree: DefaultSortingMultipleTree<String, String>

    @BeforeEach
    fun setup() {
        tree = DefaultSortingMultipleTree()
    }

    @Nested
    @DisplayName("节点添加测试")
    inner class AddNodeTests {

        @Test
        fun `添加根节点`() {
            val node = tree.addNode("root1", null, "根节点1")

            assertEquals("root1", node.key)
            assertNull(node.parentKey)
            assertEquals("根节点1", node.data)
            assertEquals("root1", node.nodePath)
            assertEquals(1L, node.sort) // 第一个根节点应该是1
            assertTrue(node.children.isEmpty())

            // 验证树中存在该节点
            val foundNode = tree.findNodeByKey("root1")
            assertNotNull(foundNode)
            assertEquals(node, foundNode)
        }

        @Test
        fun `添加多个根节点`() {
            val root1 = tree.addNode("root1", null, "根节点1")
            val root2 = tree.addNode("root2", null, "根节点2")
            val root3 = tree.addNode("root3", null, "根节点3")

            // 验证排序值递增
            assertEquals(1L, root1.sort)
            assertEquals(2L, root2.sort)
            assertEquals(3L, root3.sort)

            // 验证根节点列表
            val rootNodes = tree.getRootNodes()
            assertEquals(3, rootNodes.size)
            assertEquals(listOf(root1, root2, root3), rootNodes)
        }

        @Test
        fun `添加子节点`() {
            val root = tree.addNode("root1", null, "根节点1") // sort: 1
            val child1 = tree.addNode("child1", "root1", "子节点1") // sort: 1*100+1 = 101
            val child2 = tree.addNode("child2", "root1", "子节点2") // sort: 1*100+2 = 102

            // 验证父子关系
            assertEquals("root1", child1.parentKey)
            assertEquals("root1", child2.parentKey)

            // 验证排序值
            assertEquals(101L, child1.sort)
            assertEquals(102L, child2.sort)

            // 验证节点路径
            assertEquals("root1/child1", child1.nodePath)
            assertEquals("root1/child2", child2.nodePath)

            // 验证父节点的子节点列表
            assertEquals(2, root.children.size)
            assertTrue(root.children.contains(child1))
            assertTrue(root.children.contains(child2))

            // 通过父键查找子节点
            val childrenOfRoot = tree.findNodesByParentKey("root1")
            assertEquals(2, childrenOfRoot.size)
            assertEquals(listOf(child1, child2), childrenOfRoot)
        }

        @Test
        fun `添加多层级子节点`() {
            val root = tree.addNode("root1", null, "根节点1") // sort: 1
            val child1 = tree.addNode("child1", "root1", "子节点1") // sort: 1*100+1 = 101
            val grandChild1 = tree.addNode("grandChild1", "child1", "孙节点1") // sort: 101*100+1 = 10101

            // 验证节点路径
            assertEquals("root1", root.nodePath)
            assertEquals("root1/child1", child1.nodePath)
            assertEquals("root1/child1/grandChild1", grandChild1.nodePath)

            // 验证排序值
            assertEquals(1L, root.sort)
            assertEquals(101L, child1.sort)
            assertEquals(10101L, grandChild1.sort)

            // 验证节点关系
            assertTrue(root.children.contains(child1))
            assertTrue(child1.children.contains(grandChild1))
        }

        @Test
        fun `添加节点时指定排序值`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1") // 默认排序值
            val child2 = tree.addNode("child2", "root1", "子节点2", 2L) // 使用指定的排序值2
            val child3 = tree.addNode("child3", "root1", "子节点3", 5L) // 使用指定的排序值5

            // 验证排序值是指定值
            assertEquals(101L, child1.sort) // 默认排序值为父节点*100+1
            assertEquals(102L, child2.sort) // 父节点排序(1)*100 + 2 = 102
            // 当排序号大于生成的下一个排序号时，应该使用生成的下一个排序号
            assertEquals(103L, child3.sort) // 父节点排序(1)*100 + 3 = 103

            // 再添加一个子节点，不指定排序值，应该获得下一个排序值
            val child4 = tree.addNode("child4", "root1", "子节点4")
            assertEquals(104L, child4.sort) // 父节点排序(1)*100 + 4 = 104
        }

        @Test
        fun `添加节点处理排序冲突`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1", 1L) // sort: 1*100+1 = 101

            // 添加另一个具有相同排序值的子节点
            val child2 = tree.addNode("child2", "root1", "子节点2", 1L) // 使用了已有的排序值1

            // child1的排序值应该被递增
            assertEquals(102L, child1.sort) // 被调整为1*100+2 = 102
            assertEquals(101L, child2.sort) // 保持为1*100+1 = 101

            // 再次验证顺序
            val children = tree.findNodesByParentKey("root1")
            assertEquals(child2, children[0]) // child2应该排在前面，因为排序值更小
            assertEquals(child1, children[1]) // child1应该排在后面，因为排序值更大
        }

        @Test
        fun `添加已存在键的节点应该抛出异常`() {
            tree.addNode("key1", null, "数据1")

            val exception = assertThrows(IllegalArgumentException::class.java) {
                tree.addNode("key1", null, "数据2")
            }

            assertTrue(exception.message!!.contains("already exists"))
        }
    }

    @Nested
    @DisplayName("节点删除测试")
    inner class RemoveNodeTests {

        @Test
        fun `删除叶子节点`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val child2 = tree.addNode("child2", "root1", "子节点2")

            // 验证初始状态
            assertEquals(2, root.children.size)

            // 删除叶子节点
            val result = tree.removeNode("child1")

            // 验证删除结果
            assertTrue(result)
            assertEquals(1, root.children.size)
            assertFalse(root.children.contains(child1))
            assertTrue(root.children.contains(child2))

            // 验证节点不再存在
            assertNull(tree.findNodeByKey("child1"))

            // 验证排序值更新：child2的排序值应该前移
            assertEquals(101L, child2.sort) // 原来是102，删除了101后前移变成101
        }

        @Test
        fun `删除带有子节点的节点应递归删除`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val grandChild1 = tree.addNode("grandChild1", "child1", "孙节点1")
            val grandChild2 = tree.addNode("grandChild2", "child1", "孙节点2")

            // 验证初始状态
            assertEquals(1, root.children.size)
            assertEquals(2, child1.children.size)

            // 删除中间节点
            val result = tree.removeNode("child1")

            // 验证删除结果
            assertTrue(result)
            assertEquals(0, root.children.size)

            // 验证所有相关节点都被删除
            assertNull(tree.findNodeByKey("child1"))
            assertNull(tree.findNodeByKey("grandChild1"))
            assertNull(tree.findNodeByKey("grandChild2"))

            // 验证节点总数
            assertEquals(1, tree.getAllNodes().size)
        }

        @Test
        fun `删除根节点应递归删除整棵子树`() {
            val root1 = tree.addNode("root1", null, "根节点1")
            val root2 = tree.addNode("root2", null, "根节点2")
            tree.addNode("child1", "root1", "子节点1")

            // 删除根节点
            val result = tree.removeNode("root1")

            // 验证删除结果
            assertTrue(result)

            // 验证所有相关节点都被删除
            assertNull(tree.findNodeByKey("root1"))
            assertNull(tree.findNodeByKey("child1"))

            // 只剩下root2节点
            assertEquals(1, tree.getAllNodes().size)
            assertEquals("root2", tree.getAllNodes()[0].key)

            // root2的排序值应该前移
            assertEquals(1L, root2.sort) // 原来是2，删除了1后前移变成1
        }

        @Test
        fun `删除不存在的节点应返回false`() {
            val result = tree.removeNode("nonExistingKey")
            assertFalse(result)
        }

        @Test
        fun `删除节点后添加相同键的新节点`() {
            tree.addNode("key1", null, "原始数据")

            // 删除节点
            assertTrue(tree.removeNode("key1"))

            // 添加同键新节点
            val newNode = tree.addNode("key1", null, "新数据")

            // 验证新节点添加成功
            assertNotNull(tree.findNodeByKey("key1"))
            assertEquals("新数据", newNode.data)
            assertEquals(1L, newNode.sort) // 应该获取新的排序值
        }
    }

    @Nested
    @DisplayName("节点查询测试")
    inner class FindNodeTests {

        @Test
        fun `通过键查询节点`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child = tree.addNode("child1", "root1", "子节点1")

            val foundRoot = tree.findNodeByKey("root1")
            val foundChild = tree.findNodeByKey("child1")
            val notFound = tree.findNodeByKey("nonExisting")

            assertNotNull(foundRoot)
            assertEquals(root, foundRoot)

            assertNotNull(foundChild)
            assertEquals(child, foundChild)

            assertNull(notFound)
        }

        @Test
        fun `通过父键查询节点列表`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val child2 = tree.addNode("child2", "root1", "子节点2")
            val child3 = tree.addNode("child3", "root1", "子节点3")

            val rootChildren = tree.findNodesByParentKey("root1")
            val nonExistingChildren = tree.findNodesByParentKey("nonExisting")
            val rootNodes = tree.findNodesByParentKey(null)

            // 验证子节点列表
            assertEquals(3, rootChildren.size)
            assertTrue(rootChildren.containsAll(listOf(child1, child2, child3)))

            // 验证排序
            assertEquals(child1, rootChildren[0])
            assertEquals(child2, rootChildren[1])
            assertEquals(child3, rootChildren[2])

            // 验证不存在父键的情况
            assertTrue(nonExistingChildren.isEmpty())

            // 验证根节点查询
            assertEquals(1, rootNodes.size)
            assertEquals(root, rootNodes[0])
        }

        @Test
        fun `通过路径查询子孙节点`() {
            val root = tree.addNode("root1", null, "根节点1")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val child2 = tree.addNode("child2", "root1", "子节点2")
            val grandChild1 = tree.addNode("grandChild1", "child1", "孙节点1")
            val grandChild2 = tree.addNode("grandChild2", "child1", "孙节点2")

            // 测试根路径
            val allNodes = tree.findNodesByPath("root")
            assertEquals(5, allNodes.size)

            // 测试特定节点路径
            val child1Descendants = tree.findNodesByPath("root1/child1")
            assertEquals(3, child1Descendants.size) // child1, grandChild1, grandChild2
            assertTrue(child1Descendants.containsAll(listOf(child1, grandChild1, grandChild2)))

            // 测试叶子节点路径
            val leafNodes = tree.findNodesByPath("root1/child1/grandChild1")
            assertEquals(1, leafNodes.size)
            assertEquals(grandChild1, leafNodes[0])

            // 测试不存在的路径
            val nonExistingPath = tree.findNodesByPath("nonExistingPath")
            assertTrue(nonExistingPath.isEmpty())
        }
    }

    @Nested
    @DisplayName("树操作测试")
    inner class TreeOperationTests {

        @Test
        fun `获取所有节点`() {
            val root1 = tree.addNode("root1", null, "根节点1")
            val root2 = tree.addNode("root2", null, "根节点2")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val grandChild1 = tree.addNode("grandChild1", "child1", "孙节点1")

            val allNodes = tree.getAllNodes()

            // 验证所有节点都被返回
            assertEquals(4, allNodes.size)
            assertTrue(allNodes.containsAll(listOf(root1, root2, child1, grandChild1)))

            // 验证排序
            assertEquals(root1, allNodes[0])
            assertEquals(root2, allNodes[1])
            assertEquals(child1, allNodes[2])
            assertEquals(grandChild1, allNodes[3])
        }

        @Test
        fun `获取根节点列表`() {
            val root1 = tree.addNode("root1", null, "根节点1")
            val root2 = tree.addNode("root2", null, "根节点2")
            tree.addNode("child1", "root1", "子节点1")

            val rootNodes = tree.getRootNodes()

            // 验证只返回根节点
            assertEquals(2, rootNodes.size)
            assertTrue(rootNodes.containsAll(listOf(root1, root2)))

            // 验证排序
            assertEquals(root1, rootNodes[0])
            assertEquals(root2, rootNodes[1])
        }

        @Test
        fun `将树转换为扁平列表`() {
            val root1 = tree.addNode("root1", null, "根节点1")
            val root2 = tree.addNode("root2", null, "根节点2")
            val child1 = tree.addNode("child1", "root1", "子节点1")
            val grandChild1 = tree.addNode("grandChild1", "child1", "孙节点1")

            val flattenedTree = tree.flattenTree()

            // 验证所有节点都在列表中
            assertEquals(4, flattenedTree.size)
            assertTrue(flattenedTree.containsAll(listOf(root1, root2, child1, grandChild1)))

            // 验证按排序值排序
            assertEquals(root1, flattenedTree[0]) // sort: 1
            assertEquals(root2, flattenedTree[1]) // sort: 2
            assertEquals(child1, flattenedTree[2]) // sort: 101
            assertEquals(grandChild1, flattenedTree[3]) // sort: 10101
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    inner class ComplexScenarioTests {

        @Test
        fun `构建复杂树结构`() {
            // 创建一个包含多层级、多分支的复杂树结构
            val root = tree.addNode("root", null, "根节点")

            // 第一层子节点
            val child1 = tree.addNode("child1", "root", "子节点1")
            val child2 = tree.addNode("child2", "root", "子节点2")
            val child3 = tree.addNode("child3", "root", "子节点3")

            // 第二层子节点 - child1的子节点
            val grandChild11 = tree.addNode("gc11", "child1", "孙节点1-1")
            val grandChild12 = tree.addNode("gc12", "child1", "孙节点1-2")

            // 第二层子节点 - child2的子节点
            val grandChild21 = tree.addNode("gc21", "child2", "孙节点2-1")

            // 第三层子节点
            val greatGrandChild111 = tree.addNode("ggc111", "gc11", "曾孙节点1-1-1")

            // 验证树结构
            assertEquals(3, root.children.size)
            assertEquals(2, child1.children.size)
            assertEquals(1, child2.children.size)
            assertEquals(0, child3.children.size)
            assertEquals(1, grandChild11.children.size)

            // 验证排序值
            assertEquals(1L, root.sort)

            assertEquals(101L, child1.sort)
            assertEquals(102L, child2.sort)
            assertEquals(103L, child3.sort)

            assertEquals(10101L, grandChild11.sort)
            assertEquals(10102L, grandChild12.sort)
            assertEquals(10201L, grandChild21.sort)

            assertEquals(1010101L, greatGrandChild111.sort)

            // 验证节点路径
            assertEquals("root", root.nodePath)
            assertEquals("root/child1", child1.nodePath)
            assertEquals("root/child1/gc11", grandChild11.nodePath)
            assertEquals("root/child1/gc11/ggc111", greatGrandChild111.nodePath)

            // 验证通过路径查询
            val nodesAtRoot = tree.findNodesByPath("root")
            assertEquals(8, nodesAtRoot.size) // 所有节点

            val nodesAtChild1 = tree.findNodesByPath("root/child1")
            assertEquals(4, nodesAtChild1.size) // child1, gc11, gc12, ggc111

            val nodesAtGC11 = tree.findNodesByPath("root/child1/gc11")
            assertEquals(2, nodesAtGC11.size) // gc11, ggc111
        }

        @Test
        fun `删除中间节点并重新构建`() {
            // 构建初始树
            val root = tree.addNode("root", null, "根节点")
            val child1 = tree.addNode("child1", "root", "子节点1")
            val child2 = tree.addNode("child2", "root", "子节点2")
            val grandChild11 = tree.addNode("gc11", "child1", "孙节点1-1")
            val grandChild12 = tree.addNode("gc12", "child1", "孙节点1-2")

            // 删除中间节点child1
            tree.removeNode("child1")

            // 验证child1及其子节点都被删除
            assertNull(tree.findNodeByKey("child1"))
            assertNull(tree.findNodeByKey("gc11"))
            assertNull(tree.findNodeByKey("gc12"))

            // child2的排序值应该前移
            assertEquals(101L, child2.sort) // 原来是102，现在变成101

            // 重新添加具有相同键的节点
            val newChild1 = tree.addNode("child1", "root", "新子节点1")

            // 验证排序值是自动计算的新值
            assertEquals(102L, newChild1.sort) // 应该获取下一个可用排序值

            // 给newChild1添加子节点
            val newGrandChild = tree.addNode("newGC", "child1", "新孙节点")

            // 验证新子树构建正确
            assertEquals(10201L, newGrandChild.sort)
            assertEquals("root/child1/newGC", newGrandChild.nodePath)
        }

        @Test
        fun `处理大量节点和深层嵌套`() {
            // 构建一个有100个节点的树
            val root = tree.addNode("root", null, "根节点")

            // 添加10个子节点
            for (i in 1..10) {
                val childKey = "child$i"
                val child = tree.addNode(childKey, "root", "子节点$i")

                // 每个子节点下添加5个孙节点
                for (j in 1..5) {
                    val grandChildKey = "${childKey}_gc$j"
                    val grandChild = tree.addNode(grandChildKey, childKey, "孙节点${i}_$j")

                    // 每个孙节点下添加1个曾孙节点
                    val greatGrandChildKey = "${grandChildKey}_ggc"
                    tree.addNode(greatGrandChildKey, grandChildKey, "曾孙节点${i}_${j}")
                }
            }

            // 验证总节点数
            val allNodes = tree.getAllNodes()
            assertEquals(1 + 10 + 10*5 + 10*5*1, allNodes.size) // 根节点 + 子节点 + 孙节点 + 曾孙节点 = 1+10+50+50 = 111

            // 验证节点数据
            assertEquals("根节点", tree.findNodeByKey("root")?.data)
            assertEquals("子节点5", tree.findNodeByKey("child5")?.data)
            assertEquals("孙节点7_3", tree.findNodeByKey("child7_gc3")?.data)
            assertEquals("曾孙节点2_4", tree.findNodeByKey("child2_gc4_ggc")?.data)

            // 验证排序值和路径
            val lastChild = tree.findNodeByKey("child10")
            assertEquals(110L, lastChild?.sort)

            val lastGC = tree.findNodeByKey("child10_gc5")
            assertEquals(11005L, lastGC?.sort)
            assertEquals("root/child10/child10_gc5", lastGC?.nodePath)

            // 删除一个中间层节点，验证大量节点的删除
            tree.removeNode("child5")

            // 验证删除后的节点总数
            assertEquals(1 + 9 + 9*5 + 9*5*1, tree.getAllNodes().size) // 111 - (1+5+5) = 100
        }

        @Test
        fun `验证节点排序冲突和解决`() {
            val root = tree.addNode("root", null, "根节点")

            // 添加5个排序值连续的子节点
            val child1 = tree.addNode("child1", "root", "子节点1") // sort: 101
            val child2 = tree.addNode("child2", "root", "子节点2") // sort: 102
            val child3 = tree.addNode("child3", "root", "子节点3") // sort: 103
            val child4 = tree.addNode("child4", "root", "子节点4") // sort: 104
            val child5 = tree.addNode("child5", "root", "子节点5") // sort: 105

            // 在child2和child3之间插入一个新节点，指定排序值
            val newChild = tree.addNode("newChild", "root", "新子节点", 3L) // 希望排序值为103

            // 验证排序冲突解决：child3及之后的节点都应该后移
            assertEquals(103L, newChild.sort)
            assertEquals(104L, child3.sort) // 原来是103，变为104
            assertEquals(105L, child4.sort) // 原来是104，变为105
            assertEquals(106L, child5.sort) // 原来是105，变为106

            // 验证排序顺序正确
            val children = tree.findNodesByParentKey("root")
            assertEquals(6, children.size)
            assertEquals(child1, children[0])
            assertEquals(child2, children[1])
            assertEquals(newChild, children[2])
            assertEquals(child3, children[3])
            assertEquals(child4, children[4])
            assertEquals(child5, children[5])
        }
    }

    @Nested
    @DisplayName("类型多样性测试")
    inner class TypeVarietyTests {

        @Test
        fun `使用Integer作为键类型`() {
            val intKeyTree = DefaultSortingMultipleTree<Int, String>()

            val root = intKeyTree.addNode(1, null, "根节点")
            val child1 = intKeyTree.addNode(2, 1, "子节点1")
            val child2 = intKeyTree.addNode(3, 1, "子节点2")

            assertEquals(1, root.key)
            assertNull(root.parentKey)
            assertEquals(1, child1.parentKey)

            val foundNode = intKeyTree.findNodeByKey(2)
            assertEquals(child1, foundNode)

            val childrenOfRoot = intKeyTree.findNodesByParentKey(1)
            assertEquals(2, childrenOfRoot.size)
        }

        @Test
        fun `使用复杂对象作为数据类型`() {
            data class Person(val name: String, val age: Int)

            val personTree = DefaultSortingMultipleTree<String, Person>()

            val rootPerson = Person("张三", 50)
            val childPerson = Person("李四", 25)

            val root = personTree.addNode("person1", null, rootPerson)
            val child = personTree.addNode("person2", "person1", childPerson)

            assertEquals(rootPerson, root.data)
            assertEquals("张三", root.data.name)
            assertEquals(50, root.data.age)

            assertEquals(childPerson, child.data)
            assertEquals("李四", child.data.name)
        }
    }
}
