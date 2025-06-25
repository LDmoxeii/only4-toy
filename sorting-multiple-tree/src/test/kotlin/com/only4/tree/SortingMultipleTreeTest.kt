package com.only4.tree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * SortingMultipleNode的树构建方法单元测试
 */
class SortingMultipleNodeTest {

    @Nested
    @DisplayName("树构建方法测试")
    inner class NodeBuildingTests {

        @Test
        fun `buildNode 空节点列表测试`() {
            val nodeList = emptyList<SortingTreeNode<String, String>>()
            val result = SortingTreeNode.buildTree(nodeList) { true }
            assertTrue(result.isEmpty())
        }

        @Test
        fun `buildTree 基本功能测试`() {
            // 创建测试节点
            val root1 = DefaultSortingTreeNode("root1", "ROOT", 1L, "根节点1")
            val root2 = DefaultSortingTreeNode("root2", "ROOT", 2L, "根节点2")
            val child1 = DefaultSortingTreeNode("child1", "root1", 101L, "子节点1")
            val child2 = DefaultSortingTreeNode("child2", "root1", 102L, "子节点2")
            val grandChild = DefaultSortingTreeNode("grandChild", "child1", 10101L, "孙节点")

            val nodeList = listOf(root1, root2, child1, child2, grandChild)

            // 测试构建树
            val result = SortingTreeNode.buildTree(nodeList) { node -> node.parentKey == "ROOT" }

            // 验证结果
            assertEquals(2, result.size)
            assertEquals("root1", result[0].key)
            assertEquals("root2", result[1].key)

            // 验证子节点
            val resultRoot1 = result.find { it.key == "root1" }!!
            assertEquals(2, resultRoot1.children.size)
            assertEquals("child1", resultRoot1.children[0].key)
            assertEquals("child2", resultRoot1.children[1].key)

            // 验证孙节点
            val resultChild1 = resultRoot1.children.find { it.key == "child1" }!!
            assertEquals(1, resultChild1.children.size)
            assertEquals("grandChild", resultChild1.children[0].key)
        }

        @Test
        fun `buildTree 排序功能测试`() {
            // 创建测试节点，故意打乱顺序
            val root1 = DefaultSortingTreeNode("root1", "ROOT", 2L, "根节点1") // 排序值较大
            val root2 = DefaultSortingTreeNode("root2", "ROOT", 1L, "根节点2") // 排序值较小
            val child1 = DefaultSortingTreeNode("child1", "root1", 102L, "子节点1") // 排序值较大
            val child2 = DefaultSortingTreeNode("child2", "root1", 101L, "子节点2") // 排序值较小

            val nodeList = listOf(root1, root2, child1, child2)

            // 测试构建树
            val result = SortingTreeNode.buildTree(nodeList) { node -> node.parentKey == "ROOT" }

            // 验证根节点排序
            assertEquals(2, result.size)
            assertEquals("root2", result[0].key) // 排序值小的应该在前面
            assertEquals("root1", result[1].key)

            // 验证子节点排序
            val resultRoot1 = result.find { it.key == "root1" }!!
            assertEquals(2, resultRoot1.children.size)
            assertEquals("child2", resultRoot1.children[0].key) // 排序值小的应该在前面
            assertEquals("child1", resultRoot1.children[1].key)
        }

        @Test
        fun `buildTreeByRootKey 测试`() {
            // 创建测试节点
            val root = DefaultSortingTreeNode("root", "ROOT_KEY", 1L, "根节点")
            val child = DefaultSortingTreeNode("child", "root", 101L, "子节点")

            val nodeList = listOf(root, child)

            // 使用buildTreeByRootKey方法
            val result = SortingTreeNode.buildTreeByRootKey(nodeList, "ROOT_KEY")

            // 验证结果
            assertEquals(1, result.size)
            assertEquals("root", result[0].key)
            assertEquals(1, result[0].children.size)
            assertEquals("child", result[0].children[0].key)
        }

        @Test
        fun `buildTreeFromList 基本功能测试`() {
            // 测试数据类
            data class TestItem(val id: String, val parentId: String, val name: String)

            // 创建测试数据
            val items = listOf(
                TestItem("root1", "ROOT", "根节点1"),
                TestItem("root2", "ROOT", "根节点2"),
                TestItem("child1", "root1", "子节点1"),
                TestItem("child2", "root1", "子节点2"),
                TestItem("grandChild", "child1", "孙节点")
            )

            // 使用buildTreeFromList方法
            val result = SortingTreeNode.buildTreeFromList(
                dataList = items,
                keyExtractor = { it.id },
                parentKeyExtractor = { it.parentId },
                dataExtractor = { it.name },
                isRoot = { it.parentId == "ROOT" }
            )

            // 验证结果
            assertEquals(2, result.size)
            assertEquals("root1", result[0].key)
            assertEquals("根节点1", result[0].data)

            // 验证子节点
            val resultRoot1 = result.find { it.key == "root1" }!!
            assertEquals(2, resultRoot1.children.size)
            assertEquals("child1", resultRoot1.children[0].key)
            assertEquals("子节点1", resultRoot1.children[0].data)

            // 验证孙节点
            val resultChild1 = resultRoot1.children.find { it.key == "child1" }!!
            assertEquals(1, resultChild1.children.size)
            assertEquals("grandChild", resultChild1.children[0].key)
            assertEquals("孙节点", resultChild1.children[0].data)
        }

        @Test
        fun `buildTreeFromList 处理循环依赖测试`() {
            // 测试数据类
            data class TestItem(val id: String, val parentId: String, val name: String)

            // 创建带有循环依赖的测试数据
            val items = listOf(
                TestItem("root", "ROOT", "根节点"),
                TestItem("child", "root", "子节点"),
                TestItem("cyclic1", "cyclic2", "循环节点1"), // 循环依赖
                TestItem("cyclic2", "cyclic1", "循环节点2")  // 循环依赖
            )

            // 使用buildTreeFromList方法
            val result = SortingTreeNode.buildTreeFromList(
                dataList = items,
                keyExtractor = { it.id },
                parentKeyExtractor = { it.parentId },
                dataExtractor = { it.name },
                isRoot = { it.parentId == "ROOT" }
            )

            // 验证结果 - 应该只有正常的树结构，循环依赖的节点被忽略
            assertEquals(1, result.size)
            assertEquals("root", result[0].key)
            assertEquals(1, result[0].children.size)
            assertEquals("child", result[0].children[0].key)
        }

        @Test
        fun `buildTreeFromListByRootKey 测试`() {
            // 测试数据类
            data class TestItem(val id: String, val parentId: String, val name: String)

            // 创建测试数据
            val items = listOf(
                TestItem("root", "ROOT_KEY", "根节点"),
                TestItem("child", "root", "子节点")
            )

            // 使用buildTreeFromListByRootKey方法
            val result = SortingTreeNode.buildTreeFromListByRootKey(
                dataList = items,
                keyExtractor = { it.id },
                parentKeyExtractor = { it.parentId },
                dataExtractor = { it.name },
                rootParentKey = "ROOT_KEY"
            )

            // 验证结果
            assertEquals(1, result.size)
            assertEquals("root", result[0].key)
            assertEquals(1, result[0].children.size)
            assertEquals("child", result[0].children[0].key)
        }

        @Test
        fun `buildTreeFromList 排序值计算测试`() {
            // 测试数据类
            data class TestItem(val id: String, val parentId: String, val name: String)

            // 创建测试数据
            val items = listOf(
                TestItem("root", "ROOT", "根节点"),
                TestItem("child1", "root", "子节点1"),
                TestItem("child2", "root", "子节点2"),
                TestItem("child3", "root", "子节点3"),
                TestItem("grandChild1", "child1", "孙节点1"),
                TestItem("grandChild2", "child1", "孙节点2")
            )

            // 使用buildTreeFromList方法
            val result = SortingTreeNode.buildTreeFromList(
                dataList = items,
                keyExtractor = { it.id },
                parentKeyExtractor = { it.parentId },
                dataExtractor = { it.name },
                isRoot = { it.parentId == "ROOT" }
            )

            // 验证根节点排序值
            assertEquals(1, result[0].sort)

            // 验证子节点排序值 (父节点排序值 * 100 + 子节点索引)
            val children = result[0].children
            assertEquals(3, children.size)
            assertEquals(101, children[0].sort) // 1 * 100 + 1
            assertEquals(102, children[1].sort) // 1 * 100 + 2
            assertEquals(103, children[2].sort) // 1 * 100 + 3

            // 验证孙节点排序值
            val grandChildren = children[0].children
            assertEquals(2, grandChildren.size)
            assertEquals(10101, grandChildren[0].sort) // 101 * 100 + 1
            assertEquals(10102, grandChildren[1].sort) // 101 * 100 + 2
        }
    }
}
