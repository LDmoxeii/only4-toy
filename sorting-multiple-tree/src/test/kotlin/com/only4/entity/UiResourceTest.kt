package com.only4.entity

import org.junit.jupiter.api.*

/**
 * UiResource和UiResourceTree的单元测试类
 */
class UiResourceTest {

    private lateinit var uiResourceTree: UiResourceTree

    @BeforeEach
    fun setup() {
        uiResourceTree = UiResourceTree()
    }

    @Nested
    @DisplayName("UiResource基本测试")
    inner class UiResourceBasicTests {

        @Test
        fun `创建UiResource对象`() {
            val resourceInfo = UiResource.UiResourceInfo(
                title = "测试资源",
                enTitle = "Test Resource",
                showStatus = true,
                activeStatus = true
            )

            val resource = UiResource(
                key = "test1",
                parentKey = "",
                nodePath = "test1",
                sort = 1L,
                data = resourceInfo
            )

            Assertions.assertEquals("test1", resource.key)
            Assertions.assertEquals("", resource.parentKey)
            Assertions.assertEquals("test1", resource.nodePath)
            Assertions.assertEquals(1L, resource.sort)
            Assertions.assertEquals("测试资源", resource.data.title)
            Assertions.assertEquals("Test Resource", resource.data.enTitle)
            Assertions.assertTrue(resource.data.showStatus)
            Assertions.assertTrue(resource.data.activeStatus)
        }

        @Test
        fun `UiResourceInfo数据操作`() {
            val resourceInfo = UiResource.UiResourceInfo(
                title = "初始标题",
                enTitle = "Initial Title",
                showStatus = true,
                activeStatus = true
            )

            // 测试数据修改
            resourceInfo.title = "修改后标题"
            resourceInfo.enTitle = "Modified Title"
            resourceInfo.showStatus = false
            resourceInfo.activeStatus = false

            Assertions.assertEquals("修改后标题", resourceInfo.title)
            Assertions.assertEquals("Modified Title", resourceInfo.enTitle)
            Assertions.assertFalse(resourceInfo.showStatus)
            Assertions.assertFalse(resourceInfo.activeStatus)
        }
    }

    @Nested
    @DisplayName("UiResourceTree基本操作测试")
    inner class UiResourceTreeBasicTests {

        @Test
        fun `添加根节点`() {
            val rootInfo = UiResource.UiResourceInfo(title = "根菜单", enTitle = "Root Menu")
            val rootNode = uiResourceTree.addNode("root1", "", rootInfo)

            Assertions.assertEquals("root1", rootNode.key)
            Assertions.assertEquals("", rootNode.parentKey)
            Assertions.assertEquals("root1", rootNode.nodePath)
            Assertions.assertEquals(1L, rootNode.sort)
            Assertions.assertEquals("根菜单", rootNode.data.title)
            Assertions.assertEquals("Root Menu", rootNode.data.enTitle)

            // 验证树中存在该节点
            val foundNode = uiResourceTree.findNodeByKey("root1")
            Assertions.assertNotNull(foundNode)
            Assertions.assertEquals(rootNode, foundNode)
        }

        @Test
        fun `添加多个根节点`() {
            val root1 = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单1"))
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val root3 = uiResourceTree.addNode("root3", "", UiResource.UiResourceInfo(title = "根菜单3"))

            // 验证排序值递增
            Assertions.assertEquals(1L, root1.sort)
            Assertions.assertEquals(2L, root2.sort)
            Assertions.assertEquals(3L, root3.sort)

            // 验证根节点列表
            val rootNodes = uiResourceTree.getRootNodes()
            Assertions.assertEquals(3, rootNodes.size)
            Assertions.assertEquals(listOf(root1, root2, root3), rootNodes)
        }

        @Test
        fun `添加子节点`() {
            val rootInfo = UiResource.UiResourceInfo(title = "根菜单")
            val childInfo1 = UiResource.UiResourceInfo(title = "子菜单1")
            val childInfo2 = UiResource.UiResourceInfo(title = "子菜单2")

            val root = uiResourceTree.addRootNode("root1", rootInfo) // sort: 1
            val child1 = uiResourceTree.addNode("child1", "root1", childInfo1) // sort: 1*100+1 = 101
            val child2 = uiResourceTree.addNode("child2", "root1", childInfo2) // sort: 1*100+2 = 102

            // 验证父子关系
            Assertions.assertEquals("root1", child1.parentKey)
            Assertions.assertEquals("root1", child2.parentKey)

            // 验证排序值
            Assertions.assertEquals(101L, child1.sort)
            Assertions.assertEquals(102L, child2.sort)

            // 验证节点路径
            Assertions.assertEquals("root1", root.nodePath)
            Assertions.assertEquals("root1/child1", child1.nodePath)
            Assertions.assertEquals("root1/child2", child2.nodePath)

            // 验证父节点的子节点列表
            Assertions.assertEquals(2, root.children.size)
            Assertions.assertTrue(root.children.contains(child1))
            Assertions.assertTrue(root.children.contains(child2))

            // 通过父键查找子节点
            val childrenOfRoot = uiResourceTree.getChildren("root1")
            Assertions.assertEquals(2, childrenOfRoot.size)
            Assertions.assertEquals(listOf(child1, child2), childrenOfRoot)
        }

        @Test
        fun `添加多层级子节点`() {
            val root = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单")) // sort: 1
            val child1 =
                uiResourceTree.addNode(
                    "child1",
                    "root1",
                    UiResource.UiResourceInfo(title = "子菜单1")
                ) // sort: 1*100+1 = 101
            val grandChild1 = uiResourceTree.addNode(
                "grandChild1",
                "child1",
                UiResource.UiResourceInfo(title = "孙菜单1")
            ) // sort: 101*100+1 = 10101

            // 验证排序值
            Assertions.assertEquals(1L, root.sort)
            Assertions.assertEquals(101L, child1.sort)
            Assertions.assertEquals(10101L, grandChild1.sort)

            // 验证节点路径
            Assertions.assertEquals("root1", root.nodePath)
            Assertions.assertEquals("root1/child1", child1.nodePath)
            Assertions.assertEquals("root1/child1/grandChild1", grandChild1.nodePath)

            // 验证节点关系
            Assertions.assertTrue(root.children.contains(child1))
            Assertions.assertTrue(child1.children.contains(grandChild1))
        }
    }

    @Nested
    @DisplayName("UiResourceTree删除节点测试")
    inner class UiResourceTreeRemoveTests {

        @Test
        fun `删除叶子节点`() {
            val root = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val child2 = uiResourceTree.addNode("child2", "root1", UiResource.UiResourceInfo(title = "子菜单2"))

            // 验证初始状态
            Assertions.assertEquals(2, root.children.size)

            // 删除叶子节点
            val result = uiResourceTree.removeNode("child1")

            // 验证删除结果
            Assertions.assertTrue(result)
            Assertions.assertEquals(1, root.children.size)
            Assertions.assertFalse(root.children.contains(child1))
            Assertions.assertTrue(root.children.contains(child2))

            // 验证节点不再存在
            Assertions.assertNull(uiResourceTree.findNodeByKey("child1"))

            // 验证排序值更新：child2的排序值应该前移
            Assertions.assertEquals(101L, child2.sort) // 原来是102，删除了101后前移变成101
        }

        @Test
        fun `删除带有子节点的节点应递归删除`() {
            val root = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val grandChild1 =
                uiResourceTree.addNode("grandChild1", "child1", UiResource.UiResourceInfo(title = "孙菜单1"))
            val grandChild2 =
                uiResourceTree.addNode("grandChild2", "child1", UiResource.UiResourceInfo(title = "孙菜单2"))

            // 验证初始状态
            Assertions.assertEquals(1, root.children.size)
            Assertions.assertEquals(2, child1.children.size)

            // 删除中间节点
            val result = uiResourceTree.removeNode("child1")

            // 验证删除结果
            Assertions.assertTrue(result)
            Assertions.assertEquals(0, root.children.size)

            // 验证所有相关节点都被删除
            Assertions.assertNull(uiResourceTree.findNodeByKey("child1"))
            Assertions.assertNull(uiResourceTree.findNodeByKey("grandChild1"))
            Assertions.assertNull(uiResourceTree.findNodeByKey("grandChild2"))

            // 验证节点总数
            Assertions.assertEquals(1, uiResourceTree.flattenTree().size)
        }
    }

    @Nested
    @DisplayName("UiResourceTree移动节点测试")
    inner class UiResourceTreeMoveTests {

        @Test
        fun `移动单个节点`() {
            // 创建测试树
            val root1 = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单1"))
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val child2 = uiResourceTree.addNode("child2", "root1", UiResource.UiResourceInfo(title = "子菜单2"))

            // 移动节点
            val movedNode = uiResourceTree.moveNode(child1, "root2")

            // 验证移动结果
            Assertions.assertEquals("root2", movedNode.parentKey)
            Assertions.assertEquals("root2/child1", movedNode.nodePath)
            Assertions.assertEquals(201L, movedNode.sort) // root2的排序值(2) * 100 + 1 = 201

            // 验证原父节点的子节点列表
            Assertions.assertEquals(1, root1.children.size)
            Assertions.assertFalse(root1.children.contains(child1))
            Assertions.assertTrue(root1.children.contains(child2))

            // 验证新父节点的子节点列表
            Assertions.assertEquals(1, root2.children.size)
            Assertions.assertTrue(root2.children.contains(child1))

            // 验证原父节点下节点的排序值更新
            Assertions.assertEquals(101L, child2.sort) // 原来是102，删除了101后前移变成101
        }

        @Test
        fun `移动节点带子节点`() {
            // 创建测试树
            val root1 = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单1"))
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val grandChild1 =
                uiResourceTree.addNode("grandChild1", "child1", UiResource.UiResourceInfo(title = "孙菜单1"))

            // 移动节点
            val movedNode = uiResourceTree.moveNode(child1, "root2")

            // 验证移动结果
            Assertions.assertEquals("root2", movedNode.parentKey)
            Assertions.assertEquals("root2/child1", movedNode.nodePath)
            Assertions.assertEquals(201L, movedNode.sort) // root2的排序值(2) * 100 + 1 = 201

            // 验证子节点的路径和排序值也更新了
            val movedGrandChild = uiResourceTree.findNodeByKey("grandChild1")
            Assertions.assertNotNull(movedGrandChild)
            Assertions.assertEquals("root2/child1/grandChild1", movedGrandChild!!.nodePath)
            Assertions.assertEquals(20101L, movedGrandChild.sort) // 201 * 100 + 1 = 20101

            // 验证原父节点和新父节点的子节点列表
            Assertions.assertEquals(0, root1.children.size)
            Assertions.assertEquals(1, root2.children.size)
            Assertions.assertTrue(root2.children.contains(child1))
        }

        @Test
        fun `移动节点到指定排序位置`() {
            // 创建测试树
            val root = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val child2 = uiResourceTree.addNode("child2", "root1", UiResource.UiResourceInfo(title = "子菜单2"))
            val child3 = uiResourceTree.addNode("child3", "root1", UiResource.UiResourceInfo(title = "子菜单3"))

            // 创建另一个根节点
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val root2Child1 =
                uiResourceTree.addNode("root2Child1", "root2", UiResource.UiResourceInfo(title = "根2子菜单1"))

            // 移动节点到指定位置
            val movedNode = uiResourceTree.moveNode(child2, "root2", 2L)

            // 验证移动结果
            Assertions.assertEquals("root2", movedNode.parentKey)
            Assertions.assertEquals("root2/child2", movedNode.nodePath)
            Assertions.assertEquals(202L, movedNode.sort) // root2的排序值(2) * 100 + 2 = 202

            // 验证新父节点的子节点列表顺序
            val root2Children = uiResourceTree.getChildren("root2")
            Assertions.assertEquals(2, root2Children.size)
            Assertions.assertEquals(root2Child1, root2Children[0]) // 排序值 201
            Assertions.assertEquals(movedNode, root2Children[1]) // 排序值 202
        }

        @Test
        fun `批量移动节点`() {
            // 创建测试树
            val root1 = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单1"))
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val child2 = uiResourceTree.addNode("child2", "root1", UiResource.UiResourceInfo(title = "子菜单2"))
            val child3 = uiResourceTree.addNode("child3", "root1", UiResource.UiResourceInfo(title = "子菜单3"))

            // 批量移动节点
            val movedNodes = uiResourceTree.moveNodes(listOf("child1", "child3"), "root2")

            // 验证移动结果
            Assertions.assertEquals(2, movedNodes.size)

            // 验证节点1移动结果
            val movedChild1 = uiResourceTree.findNodeByKey("child1")
            Assertions.assertNotNull(movedChild1)
            Assertions.assertEquals("root2", movedChild1!!.parentKey)
            Assertions.assertEquals("root2/child1", movedChild1.nodePath)
            Assertions.assertEquals(201L, movedChild1.sort)

            // 验证节点3移动结果
            val movedChild3 = uiResourceTree.findNodeByKey("child3")
            Assertions.assertNotNull(movedChild3)
            Assertions.assertEquals("root2", movedChild3!!.parentKey)
            Assertions.assertEquals("root2/child3", movedChild3.nodePath)
            Assertions.assertEquals(202L, movedChild3.sort) // 应该是下一个排序值

            // 验证原父节点和新父节点的子节点列表
            Assertions.assertEquals(1, root1.children.size)
            Assertions.assertTrue(root1.children.contains(child2))

            Assertions.assertEquals(2, root2.children.size)
            Assertions.assertTrue(root2.children.contains(movedChild1))
            Assertions.assertTrue(root2.children.contains(movedChild3))
        }

        @Test
        fun `移动节点导致循环依赖时应抛出异常`() {
            // 创建测试树
            val root = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单"))
            val child = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单"))
            val grandChild =
                uiResourceTree.addNode("grandChild1", "child1", UiResource.UiResourceInfo(title = "孙菜单"))

            // 尝试将父节点移动到子节点下
            val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
                uiResourceTree.moveNode(child, "grandChild1")
            }

            Assertions.assertTrue(exception.message!!.contains("would create a cycle"))
        }
    }

    @Nested
    @DisplayName("UiResourceTree扁平化和数据更新测试")
    inner class UiResourceTreeFlattenAndUpdateTests {

        @Test
        fun `扁平化树结构`() {
            // 创建测试树
            val root1 = uiResourceTree.addNode("root1", "", UiResource.UiResourceInfo(title = "根菜单1"))
            val root2 = uiResourceTree.addNode("root2", "", UiResource.UiResourceInfo(title = "根菜单2"))
            val child1 = uiResourceTree.addNode("child1", "root1", UiResource.UiResourceInfo(title = "子菜单1"))
            val child2 = uiResourceTree.addNode("child2", "root1", UiResource.UiResourceInfo(title = "子菜单2"))
            val grandChild1 =
                uiResourceTree.addNode("grandChild1", "child1", UiResource.UiResourceInfo(title = "孙菜单1"))

            // 扁平化树结构
            val flattenedTree = uiResourceTree.flattenTree()

            // 验证扁平化结果
            Assertions.assertEquals(5, flattenedTree.size)

            // 验证排序顺序（按照sort值从小到大）
            Assertions.assertEquals(root1, flattenedTree[0]) // sort: 1
            Assertions.assertEquals(root2, flattenedTree[1]) // sort: 2
            Assertions.assertEquals(child1, flattenedTree[2]) // sort: 101
            Assertions.assertEquals(child2, flattenedTree[3]) // sort: 102
            Assertions.assertEquals(grandChild1, flattenedTree[4]) // sort: 10101
        }

        @Test
        fun `更新节点数据`() {
            // 创建测试节点
            val node = uiResourceTree.addNode(
                "test1",
                "",
                UiResource.UiResourceInfo(
                    title = "原标题",
                    enTitle = "Original Title",
                    showStatus = true,
                    activeStatus = true
                )
            )

            // 更新节点数据
            val updatedNode = uiResourceTree.updateNodeData("test1") { data ->
                data.title = "新标题"
                data.enTitle = "New Title"
                data.showStatus = false
                data.activeStatus = false
                data
            }

            // 验证更新结果
            Assertions.assertNotNull(updatedNode)
            Assertions.assertEquals("新标题", updatedNode!!.data.title)
            Assertions.assertEquals("New Title", updatedNode.data.enTitle)
            Assertions.assertFalse(updatedNode.data.showStatus)
            Assertions.assertFalse(updatedNode.data.activeStatus)

            // 验证从树中查询的结果也已更新
            val foundNode = uiResourceTree.findNodeByKey("test1")
            Assertions.assertNotNull(foundNode)
            Assertions.assertEquals("新标题", foundNode!!.data.title)
            Assertions.assertEquals("New Title", foundNode.data.enTitle)
            Assertions.assertFalse(foundNode.data.showStatus)
            Assertions.assertFalse(foundNode.data.activeStatus)
        }

        @Test
        fun `更新不存在的节点返回null`() {
            val result = uiResourceTree.updateNodeData("nonexistent") { it }
            Assertions.assertNull(result)
        }
    }

    @Nested
    @DisplayName("UiResourceTree构建测试")
    inner class UiResourceTreeBuildTests {

        @Test
        fun `从资源集合构建树`() {
            // 创建资源集合
            val resources = listOf(
                UiResource(
                    key = "root1",
                    parentKey = "",
                    nodePath = "root1",
                    sort = 1L,
                    data = UiResource.UiResourceInfo(title = "根菜单1")
                ),
                UiResource(
                    key = "root2",
                    parentKey = "",
                    nodePath = "root2",
                    sort = 2L,
                    data = UiResource.UiResourceInfo(title = "根菜单2")
                ),
                UiResource(
                    key = "child1",
                    parentKey = "root1",
                    nodePath = "root1/child1",
                    sort = 101L,
                    data = UiResource.UiResourceInfo(title = "子菜单1")
                ),
                UiResource(
                    key = "child2",
                    parentKey = "root1",
                    nodePath = "root1/child2",
                    sort = 102L,
                    data = UiResource.UiResourceInfo(title = "子菜单2")
                ),
                UiResource(
                    key = "grandChild1",
                    parentKey = "child1",
                    nodePath = "root1/child1/grandChild1",
                    sort = 10101L,
                    data = UiResource.UiResourceInfo(title = "孙菜单1")
                )
            )

            // 构建树
            val tree = UiResourceTree.buildFromResources(resources)

            // 验证树结构
            Assertions.assertEquals(5, tree.flattenTree().size)

            // 验证根节点
            val rootNodes = tree.getRootNodes()
            Assertions.assertEquals(2, rootNodes.size)

            // 验证第一个根节点的子节点
            val childrenOfRoot1 = tree.getChildren("root1")
            Assertions.assertEquals(2, childrenOfRoot1.size)

            // 验证child1节点的子节点
            val childrenOfChild1 = tree.getChildren("child1")
            Assertions.assertEquals(1, childrenOfChild1.size)

            // 验证节点关系和路径
            val root1 = tree.findNodeByKey("root1")!!
            val child1 = tree.findNodeByKey("child1")!!
            val grandChild1 = tree.findNodeByKey("grandChild1")!!

            Assertions.assertEquals("root1", root1.nodePath)
            Assertions.assertEquals("root1/child1", child1.nodePath)
            Assertions.assertEquals("root1/child1/grandChild1", grandChild1.nodePath)

            Assertions.assertTrue(root1.children.contains(child1))
            Assertions.assertTrue(child1.children.contains(grandChild1))
        }
    }
}
