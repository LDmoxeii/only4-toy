package com.only4.algorithm.structure.def

import com.only4.algorithm.structure.SortingTreeSynchronizer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * DefaultSortingTreeSynchronizer的单元测试类
 */
class DefaultSortingTreeSynchronizerTest {

    // 源树和目标树
    private lateinit var sourceTree: DefaultSortingMultipleTree<String, String>
    private lateinit var targetTree: DefaultSortingMultipleTree<String, String>

    // 树同步器
    private lateinit var synchronizer: DefaultSortingTreeSynchronizer<String, String>

    @BeforeEach
    fun setup() {
        // 创建源树和目标树，使用相同的基本配置
        sourceTree = DefaultSortingMultipleTree("ROOT", "/", 100L)
        targetTree = DefaultSortingMultipleTree("ROOT", "/", 100L)

        // 创建同步器
        val sourceMeta = SortingTreeSynchronizer.SyncMataData(
            sourceTree,
            sourceTree.dummyKey,
            sourceTree.pathSeparator,
            sourceTree.sortBase
        )
        val targetMeta = SortingTreeSynchronizer.SyncMataData(
            targetTree,
            targetTree.dummyKey,
            targetTree.pathSeparator,
            targetTree.sortBase
        )
        synchronizer = DefaultSortingTreeSynchronizer(
            { v1, v2 -> v1 == v2 },
            Pair(sourceMeta, targetMeta)
        )
    }

    @Nested
    @DisplayName("差异计算测试")
    inner class CalculateDifferencesTests {

        @Test
        fun `空树差异计算`() {
            // 两棵空树应该没有差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)
            assertTrue(differences.isEmpty())
        }

        @Test
        fun `只有源树有节点`() {
            // 在源树中添加节点
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val child1 = sourceTree.addNode("child1", "root1", "子节点1")

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要添加的节点
            assertEquals(2, differences.size)
            assertEquals(2, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.ADD })

            // 验证节点信息
            val rootDiff = differences.find { it.node.key == "root1" }
            assertNotNull(rootDiff)
            assertEquals(SortingTreeSynchronizer.SyncType.ADD, rootDiff!!.syncType)

            val childDiff = differences.find { it.node.key == "child1" }
            assertNotNull(childDiff)
            assertEquals(SortingTreeSynchronizer.SyncType.ADD, childDiff!!.syncType)
        }

        @Test
        fun `只有目标树有节点`() {
            // 在目标树中添加节点
            val root1 = targetTree.addRootNode("root1", "根节点1")
            val child1 = targetTree.addNode("child1", "root1", "子节点1")

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要删除的节点
            assertEquals(2, differences.size)
            assertEquals(2, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.DELETE })

            // 验证节点信息
            val rootDiff = differences.find { it.node.key == "root1" }
            assertNotNull(rootDiff)
            assertEquals(SortingTreeSynchronizer.SyncType.DELETE, rootDiff!!.syncType)

            val childDiff = differences.find { it.node.key == "child1" }
            assertNotNull(childDiff)
            assertEquals(SortingTreeSynchronizer.SyncType.DELETE, childDiff!!.syncType)
        }

        @Test
        fun `两树相同节点但不同属性`() {
            // 在源树和目标树中添加相同键的节点，但其他属性不同
            sourceTree.addRootNode("root1", "源树根节点", 1L)
            targetTree.addRootNode("root1", "目标树根节点", 2L) // 数据和排序值不同

            sourceTree.addNode("child1", "root1", "源树子节点")
            targetTree.addNode("child1", "root1", "目标树子节点") // 数据不同

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要更新的节点
            assertEquals(2, differences.size)
            assertEquals(2, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.UPDATE })
        }

        @Test
        fun `完全相同的两棵树`() {
            // 在源树和目标树中添加完全相同的节点
            sourceTree.addRootNode("root1", "根节点1", 1L)
            sourceTree.addNode("child1", "root1", "子节点1", 1L)

            targetTree.addRootNode("root1", "根节点1", 1L)
            targetTree.addNode("child1", "root1", "子节点1", 1L)

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个相同的节点
            assertEquals(2, differences.size)
            assertEquals(2, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.SAME })
        }

        @Test
        fun `混合差异的两棵树`() {
            // 在源树中添加节点
            sourceTree.addRootNode("root1", "根节点1", 1L) // 相同
            sourceTree.addNode("child1", "root1", "源树子节点1") // 需更新
            sourceTree.addNode("child2", "root1", "子节点2") // 需添加

            // 在目标树中添加节点
            targetTree.addRootNode("root1", "根节点1", 1L) // 相同
            targetTree.addNode("child1", "root1", "目标树子节点1") // 数据不同
            targetTree.addNode("child3", "root1", "子节点3") // 需删除

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 验证差异数量
            assertEquals(4, differences.size)
            assertEquals(1, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.SAME })
            assertEquals(1, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.ADD })
            assertEquals(1, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.UPDATE })
            assertEquals(1, differences.count { it.syncType == SortingTreeSynchronizer.SyncType.DELETE })
        }
    }

    @Nested
    @DisplayName("同步测试")
    inner class SynchronizeTests {

        @Test
        fun `同步空源树到空目标树`() {
            // 同步两棵空树
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 应该没有结果
            assertTrue(results.isEmpty())

            // 两棵树应该仍然为空
            assertTrue(sourceTree.flattenTree().isEmpty())
            assertTrue(targetTree.flattenTree().isEmpty())
        }

        @Test
        fun `同步有节点的源树到空目标树`() {
            // 在源树中添加节点
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val child1 = sourceTree.addNode("child1", "root1", "子节点1")
            val grandChild1 = sourceTree.addNode("grandChild1", "child1", "孙节点1")

            // 同步到目标树
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 验证结果
            assertEquals(3, results.size)
            assertEquals(3, results.count { it.syncType == SortingTreeSynchronizer.SyncType.ADD })

            // 验证目标树中添加的节点
            val targetRoot = targetTree.findNodeByKey("root1")
            assertNotNull(targetRoot)
            assertEquals("根节点1", targetRoot!!.data)

            val targetChild = targetTree.findNodeByKey("child1")
            assertNotNull(targetChild)
            assertEquals("子节点1", targetChild!!.data)

            val targetGrandChild = targetTree.findNodeByKey("grandChild1")
            assertNotNull(targetGrandChild)
            assertEquals("孙节点1", targetGrandChild!!.data)

            // 验证节点关系
            assertTrue(targetRoot.children.contains(targetChild))
            assertTrue(targetChild.children.contains(targetGrandChild))
        }

        @Test
        fun `同步节点移动`() {
            // 在源树中创建初始结构
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val child1 = sourceTree.addNode("child1", "root1", "子节点1")
            val child2 = sourceTree.addNode("child2", "root1", "子节点2")
            val grandChild1 = sourceTree.addNode("grandChild1", "child1", "孙节点1")

            // 同步到目标树
            synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 在源树中移动节点
            val root2 = sourceTree.addRootNode("root2", "根节点2")
            sourceTree.moveNode(grandChild1, "child2") // 将孙节点1移动到子节点2下
            sourceTree.moveNode(child1, "root2") // 将子节点1移动到根节点2下

            // 再次同步
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 验证目标树中的节点结构
            val targetRoot2 = targetTree.findNodeByKey("root2")
            assertNotNull(targetRoot2)

            val targetChild1 = targetTree.findNodeByKey("child1")
            assertNotNull(targetChild1)
            assertEquals("root2", targetChild1!!.parentKey)

            val targetChild2 = targetTree.findNodeByKey("child2")
            assertNotNull(targetChild2)

            val targetGrandChild1 = targetTree.findNodeByKey("grandChild1")
            assertNotNull(targetGrandChild1)
            assertEquals("child2", targetGrandChild1!!.parentKey)

            // 验证节点关系
            assertTrue(targetRoot2!!.children.contains(targetChild1))
            assertTrue(targetChild2!!.children.contains(targetGrandChild1))
            assertFalse(targetChild1.children.contains(targetGrandChild1))
        }

        @Test
        fun `同步子节点移动到新创建的节点`() {
            // 场景描述：
            // 1. 创建带有子节点的节点并同步
            // 2. 创建新节点，将旧节点的子节点移动到新节点下，然后删除旧节点
            // 3. 再次同步，验证结构正确

            // 步骤1：创建初始结构并同步
            val oldParent = sourceTree.addRootNode("oldParent", "旧父节点")
            val child1 = sourceTree.addNode("child1", "oldParent", "子节点1")
            val child2 = sourceTree.addNode("child2", "oldParent", "子节点2")

            synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 步骤2：创建新节点，移动子节点，删除旧节点
            val newParent = sourceTree.addRootNode("newParent", "新父节点")
            sourceTree.moveNode(child1, "newParent") // 将子节点1移动到新父节点下
            sourceTree.moveNode(child2, "newParent") // 将子节点2移动到新父节点下
            sourceTree.removeNode("oldParent") // 删除旧父节点

            // 步骤3：再次同步
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 验证目标树中的结构
            val targetNewParent = targetTree.findNodeByKey("newParent")
            assertNotNull(targetNewParent)

            val targetChild1 = targetTree.findNodeByKey("child1")
            assertNotNull(targetChild1)
            assertEquals("newParent", targetChild1!!.parentKey)

            val targetChild2 = targetTree.findNodeByKey("child2")
            assertNotNull(targetChild2)
            assertEquals("newParent", targetChild2!!.parentKey)

            // 验证旧父节点已被删除
            assertNull(targetTree.findNodeByKey("oldParent"))

            // 验证新父节点包含所有子节点
            assertEquals(2, targetNewParent!!.children.size)
            assertTrue(targetNewParent.children.contains(targetChild1))
            assertTrue(targetNewParent.children.contains(targetChild2))
        }

        @Test
        fun `选择性同步特定节点`() {
            // 在源树中添加多个节点
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val child1 = sourceTree.addNode("child1", "root1", "子节点1")
            val child2 = sourceTree.addNode("child2", "root1", "子节点2")
            val grandChild1 = sourceTree.addNode("grandChild1", "child1", "孙节点1")

            // 只同步特定节点
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("child1", "grandChild1"))

            // 验证只有指定的节点被同步
            val targetRoot1 = targetTree.findNodeByKey("root1")
            assertNotNull(targetRoot1) // 父节点也会被同步，即使未明确指定

            val targetChild1 = targetTree.findNodeByKey("child1")
            assertNotNull(targetChild1)

            val targetGrandChild1 = targetTree.findNodeByKey("grandChild1")
            assertNotNull(targetGrandChild1)

            val targetChild2 = targetTree.findNodeByKey("child2")
            assertNull(targetChild2) // 未指定同步，应为null

            // 验证节点关系
            assertTrue(targetRoot1!!.children.contains(targetChild1))
            assertTrue(targetChild1!!.children.contains(targetGrandChild1))
        }

        @Test
        fun `处理不同排序基数的树`() {
            // 创建不同排序基数的源树和目标树
            val sourceTreeDifferentBase = DefaultSortingMultipleTree<String, String>("ROOT", "/", 1000L)
            val targetTreeDifferentBase = DefaultSortingMultipleTree<String, String>("ROOT", "/", 100L)

            // 创建同步器
            val sourceMeta = SortingTreeSynchronizer.SyncMataData(
                sourceTreeDifferentBase,
                sourceTreeDifferentBase.dummyKey,
                sourceTreeDifferentBase.pathSeparator,
                sourceTreeDifferentBase.sortBase
            )
            val targetMeta = SortingTreeSynchronizer.SyncMataData(
                targetTreeDifferentBase,
                targetTreeDifferentBase.dummyKey,
                targetTreeDifferentBase.pathSeparator,
                targetTreeDifferentBase.sortBase
            )
            val syncDifferentBase = DefaultSortingTreeSynchronizer(
                { v1, v2 -> v1 == v2 },
                Pair(sourceMeta, targetMeta)
            )

            // 在源树中添加节点
            val root1 = sourceTreeDifferentBase.addRootNode("root1", "根节点1", 5L) // 排序值为1
            val child1 = sourceTreeDifferentBase.addNode("child1", "root1", "子节点1", 3L) // 排序值为1 * 1000 + 1 = 1001

            // 同步到目标树
            val results =
                syncDifferentBase.synchronizeTrees(sourceTreeDifferentBase, targetTreeDifferentBase, emptyList())

            // 验证目标树中节点的排序值
            val targetRoot = targetTreeDifferentBase.findNodeByKey("root1")
            assertNotNull(targetRoot)
            assertEquals(1L, targetRoot!!.sort) // 在目标树中，排序索引应该是1

            val targetChild = targetTreeDifferentBase.findNodeByKey("child1")
            assertNotNull(targetChild)
            assertEquals(1L, targetChild!!.sort % 100) // 在目标树中，排序索引应该是1
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    inner class EdgeCaseTests {

        @Test
        fun `同步到目标树中已存在但父节点不同的节点`() {
            // 在源树中创建节点
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val root2 = sourceTree.addRootNode("root2", "根节点2")
            val child1 = sourceTree.addNode("child1", "root1", "子节点1")

            // 在目标树中创建相同键但父节点不同的节点
            val targetRoot1 = targetTree.addRootNode("root1", "根节点1")
            val targetRoot2 = targetTree.addRootNode("root2", "根节点2")
            val targetChild1 = targetTree.addNode("child1", "root2", "子节点1") // 挂在root2下

            // 同步
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 验证子节点已移动到正确的父节点下
            val syncedChild = targetTree.findNodeByKey("child1")
            assertNotNull(syncedChild)
            assertEquals("root1", syncedChild!!.parentKey) // 应该移动到root1下

            // 验证节点关系
            assertTrue(targetTree.findNodeByKey("root1")!!.children.contains(syncedChild))
            assertFalse(targetTree.findNodeByKey("root2")!!.children.contains(syncedChild))
        }

        @Test
        fun `处理缺失父节点的子节点同步`() {
            // 在源树中创建多层级节点
            val root1 = sourceTree.addRootNode("root1", "根节点1")
            val mid1 = sourceTree.addNode("mid1", "root1", "中间节点1")
            val leaf1 = sourceTree.addNode("leaf1", "mid1", "叶节点1")

            // 只同步叶节点，应该递归同步所有必要的父节点
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("leaf1"))

            // 验证所有必要的节点都被同步
            assertNotNull(targetTree.findNodeByKey("root1"))
            assertNotNull(targetTree.findNodeByKey("mid1"))
            assertNotNull(targetTree.findNodeByKey("leaf1"))

            // 验证节点关系
            val targetRoot = targetTree.findNodeByKey("root1")!!
            val targetMid = targetTree.findNodeByKey("mid1")!!
            val targetLeaf = targetTree.findNodeByKey("leaf1")!!

            assertTrue(targetRoot.children.contains(targetMid))
            assertTrue(targetMid.children.contains(targetLeaf))
        }

        @Test
        fun `逻辑上父节点的键被替换时`() {
            // 创建初始结构并同步
            val parent = sourceTree.addRootNode("parent", "父节点")
            val child = sourceTree.addNode("child", "parent", "子节点")

            synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            val newParent = sourceTree.addRootNode("newParent", "新父节点")
            sourceTree.moveNode("child", "newParent") // 将子节点移动到新父节点下
            sourceTree.removeNode("parent")

            // 再次同步
            val results = synchronizer.synchronizeTrees(sourceTree, targetTree, emptyList())

            // 验证目标树结构
            val targetParent = targetTree.findNodeByKey("newParent")
            assertNotNull(targetParent)
            assertEquals("新父节点", targetParent!!.data) // 数据应该更新
            assertEquals(1, targetParent.children.size)

            // 新的子节点应该存在
            assertNotNull(targetTree.findNodeByKey("child"))
            assertEquals("newParent", targetTree.findNodeByKey("child")!!.parentKey)
        }
    }
}
