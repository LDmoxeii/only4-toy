package com.only4.utils

import com.only4.DefaultSortingTreeNode
import com.only4.SortingTreeNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * OrderedTreeUtils工具类的单元测试
 */
class OrderedTreeUtilsTest {

    @Nested
    @DisplayName("树结构构建测试")
    inner class BuildTreeTests {

        @Test
        fun `根据节点列表构建树 - 使用自定义判断函数`() {
            // 准备测试数据
            val node1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根节点1")
            val node2 = DefaultSortingTreeNode("2", "dummy", 2L, "2", "根节点2")
            val node3 = DefaultSortingTreeNode("3", "1", 101L, "1/3", "子节点3")
            val node4 = DefaultSortingTreeNode("4", "1", 102L, "1/4", "子节点4")
            val node5 = DefaultSortingTreeNode("5", "2", 201L, "2/5", "子节点5")
            val node6 = DefaultSortingTreeNode("6", "3", 10101L, "1/3/6", "孙节点6")

            val nodeList = listOf(node1, node2, node3, node4, node5, node6)

            // 使用自定义根节点判断函数构建树
            val rootNodes = OrderedTreeUtils.buildTree(nodeList) { it.parentKey == "dummy" }

            // 验证结果
            assertEquals(2, rootNodes.size)
            assertEquals(node1, rootNodes[0]) // 排序值为1的节点应该在前面
            assertEquals(node2, rootNodes[1]) // 排序值为2的节点应该在后面

            // 验证树结构
            assertEquals(2, node1.children.size)
            assertEquals(node3, node1.children[0])
            assertEquals(node4, node1.children[1])

            assertEquals(1, node2.children.size)
            assertEquals(node5, node2.children[0])

            assertEquals(1, node3.children.size)
            assertEquals(node6, node3.children[0])

            assertEquals(0, node4.children.size)
            assertEquals(0, node5.children.size)
            assertEquals(0, node6.children.size)
        }

        @Test
        fun `根据节点列表构建树 - 使用指定根节点键`() {
            // 准备测试数据 - 使用与上一个测试相同的数据结构
            val node1 = DefaultSortingTreeNode("1", "root", 1001L, "1", "节点1")
            val node2 = DefaultSortingTreeNode("2", "root", 1002L, "2", "节点2")
            val node3 = DefaultSortingTreeNode("3", "1", 100101L, "1/3", "节点3")
            val node4 = DefaultSortingTreeNode("4", "1", 100102L, "1/4", "节点4")
            val node5 = DefaultSortingTreeNode("5", "2", 100201L, "2/5", "节点5")
            val node6 = DefaultSortingTreeNode("6", "3", 10010101L, "1/3/6", "节点6")

            val nodeList = listOf(node1, node2, node3, node4, node5, node6)

            // 使用指定根节点键构建树
            val rootNodes = OrderedTreeUtils.buildTreeByRootKey(nodeList, "root")

            // 验证结果
            assertEquals(2, rootNodes.size)
            assertEquals(node1, rootNodes[0])
            assertEquals(node2, rootNodes[1])

            // 验证树结构
            assertEquals(2, node1.children.size)
            assertEquals(1, node2.children.size)
            assertEquals(1, node3.children.size)
        }

        @Test
        fun `处理空节点列表`() {
            val emptyList = emptyList<SortingTreeNode<String, String>>()
            val result = OrderedTreeUtils.buildTree(emptyList) { it.parentKey == "dummy" }
            assertTrue(result.isEmpty())
        }

        @Test
        fun `处理没有根节点的列表`() {
            // 所有节点都有父节点，没有根节点
            val node1 = DefaultSortingTreeNode("1", "parent", 1L, "1", "节点1")
            val node2 = DefaultSortingTreeNode("2", "parent", 2L, "2", "节点2")

            val nodeList = listOf(node1, node2)
            val result = OrderedTreeUtils.buildTree(nodeList) { it.parentKey == "dummy" }
            assertTrue(result.isEmpty())
        }

        @Test
        fun `处理循环依赖`() {
            // 创建循环依赖： A → B → C → A
            val nodeA = DefaultSortingTreeNode("A", "C", 1L, "A", "节点A")
            val nodeB = DefaultSortingTreeNode("B", "A", 2L, "B", "节点B")
            val nodeC = DefaultSortingTreeNode("C", "B", 3L, "C", "节点C")
            val nodeRoot = DefaultSortingTreeNode("root", "dummy", 0L, "root", "根节点")

            val nodeList = listOf(nodeA, nodeB, nodeC, nodeRoot)
            val result = OrderedTreeUtils.buildTree(nodeList) { it.parentKey == "dummy" }

            // 验证只有根节点被正确处理
            assertEquals(1, result.size)
            assertEquals(nodeRoot, result[0])
            // 循环依赖的节点没有被添加到根节点的子节点中
            assertEquals(0, nodeRoot.children.size)
        }

        @Test
        fun `处理复杂树结构 - 具有多级子节点`() {
            // 构建一个较复杂的树结构，包含多级子节点
            val root1 = DefaultSortingTreeNode("root1", "dummy", 1L, "root1", "根1")
            val root2 = DefaultSortingTreeNode("root2", "dummy", 2L, "root2", "根2")

            val child1 = DefaultSortingTreeNode("child1", "root1", 101L, "root1/child1", "子1")
            val child2 = DefaultSortingTreeNode("child2", "root1", 102L, "root1/child2", "子2")
            val child3 = DefaultSortingTreeNode("child3", "root2", 201L, "root2/child3", "子3")

            val grandchild1 = DefaultSortingTreeNode("gc1", "child1", 10101L, "root1/child1/gc1", "孙1")
            val grandchild2 = DefaultSortingTreeNode("gc2", "child1", 10102L, "root1/child1/gc2", "孙2")
            val grandchild3 = DefaultSortingTreeNode("gc3", "child2", 10201L, "root1/child2/gc3", "孙3")

            val greatgrandchild1 = DefaultSortingTreeNode("ggc1", "gc1", 1010101L, "root1/child1/gc1/ggc1", "曾孙1")

            // 打乱顺序添加到列表中
            val nodeList = listOf(
                child2, grandchild1, greatgrandchild1, root2,
                child1, root1, grandchild3, child3, grandchild2
            )

            val result = OrderedTreeUtils.buildTree(nodeList) { it.parentKey == "dummy" }

            // 验证根节点
            assertEquals(2, result.size)
            assertEquals(root1, result[0])
            assertEquals(root2, result[1])

            // 验证第一层子节点
            assertEquals(2, root1.children.size)
            assertEquals(child1, root1.children[0])
            assertEquals(child2, root1.children[1])

            assertEquals(1, root2.children.size)
            assertEquals(child3, root2.children[0])

            // 验证第二层子节点
            assertEquals(2, child1.children.size)
            assertEquals(grandchild1, child1.children[0])
            assertEquals(grandchild2, child1.children[1])

            assertEquals(1, child2.children.size)
            assertEquals(grandchild3, child2.children[0])

            // 验证第三层子节点
            assertEquals(1, grandchild1.children.size)
            assertEquals(greatgrandchild1, grandchild1.children[0])
        }
    }

    @Nested
    @DisplayName("从列表构建树测试")
    inner class BuildTreeFromListTests {

        @Test
        fun `从列表构建树 - 使用自定义判断函数`() {
            // 准备测试数据
            val items = listOf(
                TestItem("1", "dummy", "根节点1"),
                TestItem("2", "dummy", "根节点2"),
                TestItem("3", "1", "子节点3"),
                TestItem("4", "1", "子节点4"),
                TestItem("5", "2", "子节点5"),
                TestItem("6", "3", "孙节点6")
            )

            // 构建树
            val rootNodes = OrderedTreeUtils.buildTreeFromList(
                items,
                { it.id },
                { it.parentId },
                { it.name },
                { it.parentId == "dummy" }
            )

            // 验证结果
            assertEquals(2, rootNodes.size)

            // 验证根节点
            val root1 = rootNodes[0]
            val root2 = rootNodes[1]
            assertEquals("1", root1.key)
            assertEquals("根节点1", root1.data)
            assertEquals("2", root2.key)
            assertEquals("根节点2", root2.data)

            // 验证树结构
            assertEquals(2, root1.children.size)
            val child3 = root1.children[0]
            val child4 = root1.children[1]
            assertEquals("3", child3.key)
            assertEquals("子节点3", child3.data)
            assertEquals("4", child4.key)
            assertEquals("子节点4", child4.data)

            assertEquals(1, root2.children.size)
            val child5 = root2.children[0]
            assertEquals("5", child5.key)
            assertEquals("子节点5", child5.data)

            assertEquals(1, child3.children.size)
            val grandchild6 = child3.children[0]
            assertEquals("6", grandchild6.key)
            assertEquals("孙节点6", grandchild6.data)
        }

        @Test
        fun `从列表构建树 - 使用指定根节点键`() {
            // 准备测试数据
            val items = listOf(
                TestItem("1", "root", "节点1"),
                TestItem("2", "root", "节点2"),
                TestItem("3", "1", "节点3"),
                TestItem("4", "1", "节点4"),
                TestItem("5", "2", "节点5"),
                TestItem("6", "3", "节点6")
            )

            // 构建树
            val rootNodes = OrderedTreeUtils.buildTreeFromListByRootKey(
                items,
                { it.id },
                { it.parentId },
                { it.name },
                "root"
            )

            // 验证结果
            assertEquals(2, rootNodes.size)
            assertEquals("1", rootNodes[0].key)
            assertEquals("2", rootNodes[1].key)

            // 验证排序值和路径
            assertEquals(1L, rootNodes[0].sort)
            assertEquals("1", rootNodes[0].nodePath)
            assertEquals(2L, rootNodes[1].sort)
            assertEquals("2", rootNodes[1].nodePath)

            // 验证子节点的排序值和路径
            val child3 = rootNodes[0].children[0]
            assertEquals(101L, child3.sort)
            assertEquals("1/3", child3.nodePath)

            val child6 = rootNodes[0].children[0].children[0]
            assertEquals(10101L, child6.sort)
            assertEquals("1/3/6", child6.nodePath)
        }

        @Test
        fun `处理空列表`() {
            val emptyList = emptyList<TestItem>()
            val result = OrderedTreeUtils.buildTreeFromList<String, String, TestItem>(
                emptyList,
                { it.id },
                { it.parentId },
                { it.name },
                { it.parentId == "dummy" }
            )
            assertTrue(result.isEmpty())
        }

        @Test
        fun `处理循环依赖的列表`() {
            // 创建循环依赖： A → B → C → A
            val items = listOf(
                TestItem("A", "C", "节点A"),
                TestItem("B", "A", "节点B"),
                TestItem("C", "B", "节点C"),
                TestItem("root", "dummy", "根节点")
            )

            val result = OrderedTreeUtils.buildTreeFromList(
                items,
                { it.id },
                { it.parentId },
                { it.name },
                { it.parentId == "dummy" }
            )

            // 验证只有根节点被正确处理
            assertEquals(1, result.size)
            assertEquals("root", result[0].key)
            // 循环依赖的节点没有被添加到根节点的子节点中
            assertEquals(0, result[0].children.size)
        }

        @Test
        fun `处理复杂数据结构`() {
            // 定义一个更复杂的数据结构
            data class ComplexItem(
                val id: Int,
                val parentId: Int?,
                val name: String,
                val attributes: Map<String, Any>
            )

            val items = listOf(
                ComplexItem(1, 0, "Root1", mapOf("type" to "folder", "created" to "2023-01-01")),
                ComplexItem(2, 0, "Root2", mapOf("type" to "folder", "created" to "2023-01-02")),
                ComplexItem(3, 1, "Child1", mapOf("type" to "file", "size" to 100)),
                ComplexItem(4, 1, "Child2", mapOf("type" to "folder", "created" to "2023-01-03")),
                ComplexItem(5, 2, "Child3", mapOf("type" to "file", "size" to 200)),
                ComplexItem(6, 4, "Grandchild1", mapOf("type" to "file", "size" to 50))
            )

            val result = OrderedTreeUtils.buildTreeFromList(
                items,
                { it.id },
                { it.parentId },
                { it.attributes + ("name" to it.name) },
                { it.parentId == 0 }
            )

            // 验证结果
            assertEquals(2, result.size)

            // 验证复杂数据内容
            val rootNode = result[0]

            @Suppress("UNCHECKED_CAST")
            val rootData = rootNode.data as Map<String, Any>
            assertEquals("folder", rootData["type"])
            assertEquals("Root1", rootData["name"])

            // 验证嵌套结构
            assertEquals(2, rootNode.children.size)
            val childNode = rootNode.children[0]

            @Suppress("UNCHECKED_CAST")
            val childData = childNode.data as Map<String, Any>
            assertEquals("file", childData["type"])
            assertEquals(100, childData["size"])
        }
    }

    @Nested
    @DisplayName("查找节点测试")
    inner class FindNodeTests {

        @Test
        fun `通过键查找节点`() {
            // 构建测试树
            val root1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val root2 = DefaultSortingTreeNode("2", "dummy", 2L, "2", "根2")
            val child1 = DefaultSortingTreeNode("3", "1", 101L, "1/3", "子1")
            val child2 = DefaultSortingTreeNode("4", "1", 102L, "1/4", "子2")
            val grandchild = DefaultSortingTreeNode("5", "3", 10101L, "1/3/5", "孙1")

            root1.children.addAll(listOf(child1, child2))
            child1.children.add(grandchild)

            val rootNodes = listOf(root1, root2)

            // 测试查找节点
            val foundRoot = OrderedTreeUtils.findNodeByKey("1", rootNodes)
            val foundChild = OrderedTreeUtils.findNodeByKey("3", rootNodes)
            val foundGrandchild = OrderedTreeUtils.findNodeByKey("5", rootNodes)
            val notFound = OrderedTreeUtils.findNodeByKey("999", rootNodes)

            // 验证结果
            assertEquals(root1, foundRoot)
            assertEquals(child1, foundChild)
            assertEquals(grandchild, foundGrandchild)
            assertNull(notFound)
        }

        @Test
        fun `通过路径查找节点`() {
            // 构建测试树
            val root1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val root2 = DefaultSortingTreeNode("2", "dummy", 2L, "2", "根2")
            val child1 = DefaultSortingTreeNode("3", "1", 101L, "1/3", "子1")
            val child2 = DefaultSortingTreeNode("4", "1", 102L, "1/4", "子2")
            val grandchild = DefaultSortingTreeNode("5", "3", 10101L, "1/3/5", "孙1")

            root1.children.addAll(listOf(child1, child2))
            child1.children.add(grandchild)

            val rootNodes = listOf(root1, root2)

            // 测试查找节点
            val foundRoot = OrderedTreeUtils.findNodeByPath("1", rootNodes)
            val foundChild = OrderedTreeUtils.findNodeByPath("1/3", rootNodes)
            val foundGrandchild = OrderedTreeUtils.findNodeByPath("1/3/5", rootNodes)
            val notFound = OrderedTreeUtils.findNodeByPath("nonexistent", rootNodes)

            // 验证结果
            assertEquals(root1, foundRoot)
            assertEquals(child1, foundChild)
            assertEquals(grandchild, foundGrandchild)
            assertNull(notFound)
        }
    }

    @Nested
    @DisplayName("树结构操作测试")
    inner class TreeOperationTests {

        @Test
        fun `将树扁平化为列表`() {
            // 构建测试树
            val root1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val root2 = DefaultSortingTreeNode("2", "dummy", 2L, "2", "根2")
            val child1 = DefaultSortingTreeNode("3", "1", 101L, "1/3", "子1")
            val child2 = DefaultSortingTreeNode("4", "1", 102L, "1/4", "子2")
            val child3 = DefaultSortingTreeNode("5", "2", 201L, "2/5", "子3")
            val grandchild = DefaultSortingTreeNode("6", "3", 10101L, "1/3/6", "孙1")

            root1.children.addAll(listOf(child1, child2))
            root2.children.add(child3)
            child1.children.add(grandchild)

            val rootNodes = listOf(root1, root2)

            // 扁平化树
            val flattenedList = OrderedTreeUtils.flattenTree(rootNodes)

            // 验证结果
            assertEquals(6, flattenedList.size)

            // 验证扁平化后按排序值排序
            assertEquals(root1, flattenedList[0]) // sort: 1
            assertEquals(root2, flattenedList[1]) // sort: 2
            assertEquals(child1, flattenedList[2]) // sort: 101
            assertEquals(child2, flattenedList[3]) // sort: 102
            assertEquals(child3, flattenedList[4]) // sort: 201
            assertEquals(grandchild, flattenedList[5]) // sort: 10101
        }

        @Test
        fun `深度优先遍历扁平化树`() {
            // 构建测试树
            val root1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val child1 = DefaultSortingTreeNode("2", "1", 101L, "1/2", "子1")
            val child2 = DefaultSortingTreeNode("3", "1", 102L, "1/3", "子2")
            val grandchild1 = DefaultSortingTreeNode("4", "2", 10101L, "1/2/4", "孙1")
            val grandchild2 = DefaultSortingTreeNode("5", "3", 10201L, "1/3/5", "孙2")

            // 构建树结构
            root1.children.addAll(listOf(child1, child2))
            child1.children.add(grandchild1)
            child2.children.add(grandchild2)

            val rootNodes = listOf(root1)

            // 使用深度优先遍历扁平化
            val depthFirstList = OrderedTreeUtils.flattenTree(rootNodes, OrderedTreeUtils.TraversalType.DEPTH_FIRST)

            // 验证结果 - 深度优先应该是：根1 -> 子1 -> 孙1 -> 子2 -> 孙2
            assertEquals(5, depthFirstList.size)
            assertEquals(root1, depthFirstList[0])
            assertEquals(child1, depthFirstList[1])
            assertEquals(grandchild1, depthFirstList[2])
            assertEquals(child2, depthFirstList[3])
            assertEquals(grandchild2, depthFirstList[4])
        }

        @Test
        fun `广度优先遍历扁平化树`() {
            // 构建测试树
            val root1 = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val child1 = DefaultSortingTreeNode("2", "1", 101L, "1/2", "子1")
            val child2 = DefaultSortingTreeNode("3", "1", 102L, "1/3", "子2")
            val grandchild1 = DefaultSortingTreeNode("4", "2", 10101L, "1/2/4", "孙1")
            val grandchild2 = DefaultSortingTreeNode("5", "3", 10201L, "1/3/5", "孙2")

            // 构建树结构
            root1.children.addAll(listOf(child1, child2))
            child1.children.add(grandchild1)
            child2.children.add(grandchild2)

            val rootNodes = listOf(root1)

            // 使用广度优先遍历扁平化
            val breadthFirstList = OrderedTreeUtils.flattenTree(rootNodes, OrderedTreeUtils.TraversalType.BREADTH_FIRST)

            // 验证结果 - 广度优先应该是：根1 -> 子1 -> 子2 -> 孙1 -> 孙2
            assertEquals(5, breadthFirstList.size)
            assertEquals(root1, breadthFirstList[0])
            assertEquals(child1, breadthFirstList[1])
            assertEquals(child2, breadthFirstList[2])
            assertEquals(grandchild1, breadthFirstList[3])
            assertEquals(grandchild2, breadthFirstList[4])
        }

        @Test
        fun `复杂树结构的不同遍历方式比较`() {
            // 构建一个更复杂的树结构
            //       A
            //     /   \
            //    B     C
            //   / \   / \
            //  D   E F   G
            //     /
            //    H

            val nodeA = DefaultSortingTreeNode("A", "dummy", 1L, "A", "节点A")
            val nodeB = DefaultSortingTreeNode("B", "A", 101L, "A/B", "节点B")
            val nodeC = DefaultSortingTreeNode("C", "A", 102L, "A/C", "节点C")
            val nodeD = DefaultSortingTreeNode("D", "B", 10101L, "A/B/D", "节点D")
            val nodeE = DefaultSortingTreeNode("E", "B", 10102L, "A/B/E", "节点E")
            val nodeF = DefaultSortingTreeNode("F", "C", 10201L, "A/C/F", "节点F")
            val nodeG = DefaultSortingTreeNode("G", "C", 10202L, "A/C/G", "节点G")
            val nodeH = DefaultSortingTreeNode("H", "E", 1010201L, "A/B/E/H", "节点H")

            // 构建树结构
            nodeA.children.addAll(listOf(nodeB, nodeC))
            nodeB.children.addAll(listOf(nodeD, nodeE))
            nodeC.children.addAll(listOf(nodeF, nodeG))
            nodeE.children.add(nodeH)

            val rootNodes = listOf(nodeA)

            // 深度优先遍历
            val depthFirstList = OrderedTreeUtils.flattenTree(rootNodes, OrderedTreeUtils.TraversalType.DEPTH_FIRST)

            // 广度优先遍历
            val breadthFirstList = OrderedTreeUtils.flattenTree(rootNodes, OrderedTreeUtils.TraversalType.BREADTH_FIRST)

            // 验证深度优先结果: A -> B -> D -> E -> H -> C -> F -> G
            assertEquals(8, depthFirstList.size)
            assertEquals(nodeA, depthFirstList[0])
            assertEquals(nodeB, depthFirstList[1])
            assertEquals(nodeD, depthFirstList[2])
            assertEquals(nodeE, depthFirstList[3])
            assertEquals(nodeH, depthFirstList[4])
            assertEquals(nodeC, depthFirstList[5])
            assertEquals(nodeF, depthFirstList[6])
            assertEquals(nodeG, depthFirstList[7])

            // 验证广度优先结果: A -> B -> C -> D -> E -> F -> G -> H
            assertEquals(8, breadthFirstList.size)
            assertEquals(nodeA, breadthFirstList[0])
            assertEquals(nodeB, breadthFirstList[1])
            assertEquals(nodeC, breadthFirstList[2])
            assertEquals(nodeD, breadthFirstList[3])
            assertEquals(nodeE, breadthFirstList[4])
            assertEquals(nodeF, breadthFirstList[5])
            assertEquals(nodeG, breadthFirstList[6])
            assertEquals(nodeH, breadthFirstList[7])
        }

        @Test
        fun `获取节点及其所有子孙节点`() {
            // 构建测试树
            val root = DefaultSortingTreeNode("1", "dummy", 1L, "1", "根1")
            val child1 = DefaultSortingTreeNode("2", "1", 101L, "1/2", "子1")
            val child2 = DefaultSortingTreeNode("3", "1", 102L, "1/3", "子2")
            val grandchild1 = DefaultSortingTreeNode("4", "2", 10101L, "1/2/4", "孙1")
            val grandchild2 = DefaultSortingTreeNode("5", "2", 10102L, "1/2/5", "孙2")
            val greatgrandchild = DefaultSortingTreeNode("6", "4", 1010101L, "1/2/4/6", "曾孙1")

            root.children.addAll(listOf(child1, child2))
            child1.children.addAll(listOf(grandchild1, grandchild2))
            grandchild1.children.add(greatgrandchild)

            // 从子1节点开始获取子孙节点
            val descendants = OrderedTreeUtils.getNodeAndDescendants(child1)

            // 验证结果
            assertEquals(4, descendants.size)

            // 验证包含所有子孙节点
            assertTrue(descendants.contains(child1))
            assertTrue(descendants.contains(grandchild1))
            assertTrue(descendants.contains(grandchild2))
            assertTrue(descendants.contains(greatgrandchild))

            // 验证不包含非子孙节点
            assertFalse(descendants.contains(root))
            assertFalse(descendants.contains(child2))
        }
    }
}

// 测试用的数据类
data class TestItem(val id: String, val parentId: String, val name: String)
