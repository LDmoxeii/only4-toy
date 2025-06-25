package com.only4.synchronizer

import com.only4.tree.DefaultSortingMultipleTree
import org.junit.jupiter.api.*

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
        sourceTree = DefaultSortingMultipleTree("ROOT", 100L)
        targetTree = DefaultSortingMultipleTree("ROOT", 100L)

        // 创建同步器，使用默认的值比较器
        synchronizer = DefaultSortingTreeSynchronizer { v1, v2 -> v1 == v2 }
    }

    @Nested
    @DisplayName("差异计算测试")
    inner class CalculateDifferencesTests {

        @Test
        fun `空树差异计算`() {
            // 两棵空树应该没有差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)
            Assertions.assertTrue(differences.isEmpty())
        }

        @Test
        fun `只有源树有节点`() {
            // 在源树中添加节点
            sourceTree.addRootNode("root1", "根节点1")
            sourceTree.addNode("child1", "root1", "子节点1")

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要添加的节点
            Assertions.assertEquals(2, differences.size)
            Assertions.assertTrue(differences.all { it.syncType == SortingTreeSynchronizer.SyncType.ADD })
        }

        @Test
        fun `只有目标树有节点`() {
            // 在目标树中添加节点
            targetTree.addRootNode("root1", "根节点1")
            targetTree.addNode("child1", "root1", "子节点1")

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要删除的节点
            Assertions.assertEquals(2, differences.size)
            Assertions.assertTrue(differences.all { it.syncType == SortingTreeSynchronizer.SyncType.DELETE })
        }

        @Test
        fun `两树相同节点但数据不同`() {
            // 在源树和目标树中添加相同键的节点，但其他属性不同
            sourceTree.addRootNode("root1", "源树根节点", 1L)
            targetTree.addRootNode("root1", "目标树根节点", 2L) // 数据和排序值不同

            sourceTree.addNode("child1", "root1", "源树子节点")
            targetTree.addNode("child1", "root1", "目标树子节点") // 数据不同

            // 计算差异
            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

            // 应该有两个需要更新的节点
            Assertions.assertEquals(2, differences.size)
            Assertions.assertTrue(differences.all { it.syncType == SortingTreeSynchronizer.SyncType.UPDATE })
        }

        @Test
        fun `两树节点位置不同`() {
            // 源树结构
            sourceTree.addRootNode("root1", "根节点1")
            sourceTree.addNode("child1", "root1", "子节点")

            // 目标树结构，子节点父节点不同
            targetTree.addRootNode("root1", "根节点1")
            targetTree.addRootNode("root2", "根节点2")
            targetTree.addNode("child1", "root2", "子节点")

            val differences = synchronizer.calculateDifferences(sourceTree, targetTree)
            Assertions.assertEquals(3, differences.size)

            val childDiff = differences.find { it.node.key == "child1" }
            Assertions.assertNotNull(childDiff)
            // 位置不同也被认为是更新
            Assertions.assertEquals(SortingTreeSynchronizer.SyncType.UPDATE, childDiff!!.syncType)
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
            Assertions.assertEquals(2, differences.size)
            Assertions.assertTrue(differences.all { it.syncType == SortingTreeSynchronizer.SyncType.SAME })
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
            Assertions.assertEquals(4, differences.size)
            Assertions.assertEquals(
                1,
                differences.count { it.syncType == SortingTreeSynchronizer.SyncType.SAME && it.node.key == "root1" })
            Assertions.assertEquals(
                1,
                differences.count { it.syncType == SortingTreeSynchronizer.SyncType.ADD && it.node.key == "child2" })
            Assertions.assertEquals(
                1,
                differences.count { it.syncType == SortingTreeSynchronizer.SyncType.UPDATE && it.node.key == "child1" })
            Assertions.assertEquals(
                1,
                differences.count { it.syncType == SortingTreeSynchronizer.SyncType.DELETE && it.node.key == "child3" })
        }
    }

    @Nested
    @DisplayName("同步测试")
    inner class SynchronizeTests {

        @Test
        fun `从有节点的源树同步到空目标树`() {
            // 在源树中添加节点
            sourceTree.addRootNode("root1", "根节点1")
            sourceTree.addNode("child1", "root1", "子节点1")
            sourceTree.addNode("grandChild1", "child1", "孙节点1")

            // 同步到目标树
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证目标树与源树结构一致
            Assertions.assertEquals(sourceTree.flattenTree().size, targetTree.flattenTree().size)
            Assertions.assertNotNull(targetTree.findNodeByKey("grandChild1"))
        }

        @Test
        fun `从空源树同步到有节点的目标树（完全删除）`() {
            // 在目标树中添加节点
            targetTree.addRootNode("root1", "根节点1")
            targetTree.addNode("child1", "root1", "子节点1")

            // 同步
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 目标树应该变为空
            Assertions.assertTrue(targetTree.flattenTree().isEmpty())
        }

        @Test
        fun `同步节点数据更新`() {
            // 初始结构
            sourceTree.addRootNode("root1", "数据v1")
            targetTree.addRootNode("root1", "数据v2")

            // 同步
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证数据已更新
            Assertions.assertEquals("数据v1", targetTree.findNodeByKey("root1")?.data)
        }

        @Test
        fun `同步节点移动`() {
            // 初始结构并同步
            sourceTree.addRootNode("root1", "根节点1")
            sourceTree.addRootNode("root2", "根节点2")
            sourceTree.addNode("child1", "root1", "子节点1")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 在源树中移动节点
            sourceTree.moveNode("child1", "root2")

            // 再次同步
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证目标树中的节点已移动
            val targetChild = targetTree.findNodeByKey("child1")
            Assertions.assertNotNull(targetChild)
            Assertions.assertEquals("root2", targetChild!!.parentKey)
            Assertions.assertTrue(targetTree.findNodeByKey("root2")!!.children.contains(targetChild))
            Assertions.assertFalse(targetTree.findNodeByKey("root1")!!.children.contains(targetChild))
        }

        @Test
        fun `同步节点排序变化`() {
            // 初始结构并同步
            sourceTree.addRootNode("root1", "根节点")
            sourceTree.addNode("child1", "root1", "子1", 1L)
            sourceTree.addNode("child2", "root1", "子2", 2L)
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证初始顺序
            var parent = targetTree.findNodeByKey("root1")!!
            Assertions.assertEquals("child1", parent.children.first().key)

            // 修改源树排序并再次同步
            sourceTree.findNodeByKey("child1")!!.sort = 3L // child1排到child2后面
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证目标树顺序已更新
            parent = targetTree.findNodeByKey("root1")!!
            Assertions.assertEquals("child2", parent.children.first().key)
            Assertions.assertEquals("child1", parent.children.last().key)
        }

        @Test
        fun `同步整个子树移动`() {
            // 初始结构并同步
            sourceTree.addRootNode("root1", "根1")
            sourceTree.addRootNode("root2", "根2")
            val subTreeRoot = sourceTree.addNode("subTreeRoot", "root1", "子树根")
            val subTreeChild = sourceTree.addNode("subTreeChild", "subTreeRoot", "子树的子节点")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 移动整个子树
            sourceTree.moveNode(subTreeRoot, "root2")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证子树已移动
            val targetSubTreeRoot = targetTree.findNodeByKey("subTreeRoot")
            Assertions.assertEquals("root2", targetSubTreeRoot?.parentKey)
            Assertions.assertNotNull(targetTree.findNodeByKey("subTreeChild"))
            Assertions.assertEquals("subTreeRoot", targetTree.findNodeByKey("subTreeChild")?.parentKey)
            Assertions.assertEquals(1, targetSubTreeRoot!!.children.size)
        }

        @Test
        fun `同步中父节点被删除且子节点被重新挂载`() {
            // 场景：parent被删除，其子节点child被移动到root下
            // 初始结构并同步
            val root = sourceTree.addRootNode("root", "根")
            val parent = sourceTree.addNode("parent", "root", "父")
            val child = sourceTree.addNode("child", "parent", "子")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 修改源树：删除parent，将child移动到root下
            sourceTree.moveNode(child, "root")
            sourceTree.removeNode("parent")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证目标树
            Assertions.assertNull(targetTree.findNodeByKey("parent"))
            val targetChild = targetTree.findNodeByKey("child")
            Assertions.assertNotNull(targetChild)
            Assertions.assertEquals("root", targetChild?.parentKey)
            Assertions.assertTrue(targetTree.findNodeByKey("root")!!.children.contains(targetChild))
        }

        @Test
        fun `同步带有子节点的节点删除`() {
            // 初始结构并同步
            sourceTree.addRootNode("root1", "根1")
            val toBeDeleted = sourceTree.addNode("toBeDeleted", "root1", "待删除节点")
            sourceTree.addNode("childOfDeleted", "toBeDeleted", "待删除的子节点")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            Assertions.assertNotNull(targetTree.findNodeByKey("toBeDeleted"))
            Assertions.assertNotNull(targetTree.findNodeByKey("childOfDeleted"))

            // 删除节点
            sourceTree.removeNode("toBeDeleted")
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证节点及其子节点都已被删除
            Assertions.assertNull(targetTree.findNodeByKey("toBeDeleted"))
            Assertions.assertNull(targetTree.findNodeByKey("childOfDeleted"))
        }

        @Test
        fun `选择性同步`() {
            // 在源树中添加多个节点
            sourceTree.addRootNode("root1", "根节点1")
            sourceTree.addNode("child1", "root1", "子节点1")
            sourceTree.addNode("grandChild1", "child1", "孙节点1")
            sourceTree.addNode("child2", "root1", "子节点2")

            // 只同步特定节点及其必要的父节点
            synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("child1", "grandChild1"))

            // 验证只有指定的节点及其父节点被同步
            Assertions.assertNotNull(targetTree.findNodeByKey("root1"))
            Assertions.assertNotNull(targetTree.findNodeByKey("child1"))
            Assertions.assertNotNull(targetTree.findNodeByKey("grandChild1"))
            Assertions.assertNull(targetTree.findNodeByKey("child2")) // 未指定同步，不应存在
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    inner class EdgeCaseTests {

        @Test
        fun `同步到目标树中已存在但父节点不同的节点`() {
            // 源树
            sourceTree.addRootNode("root1", "根1")
            sourceTree.addRootNode("root2", "根2")
            sourceTree.addNode("child1", "root1", "子节点")

            // 目标树，child1有不同的父节点
            targetTree.addRootNode("root1", "根1")
            targetTree.addRootNode("root2", "根2")
            targetTree.addNode("child1", "root2", "子节点")

            // 同步
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证子节点已移动到正确的父节点下
            val syncedChild = targetTree.findNodeByKey("child1")
            Assertions.assertEquals("root1", syncedChild?.parentKey)
            Assertions.assertTrue(targetTree.findNodeByKey("root1")!!.children.contains(syncedChild))
            Assertions.assertFalse(targetTree.findNodeByKey("root2")!!.children.contains(syncedChild!!))
        }

        @Test
        fun `处理缺失父节点的子节点选择性同步`() {
            // 在源树中创建多层级节点
            sourceTree.addRootNode("root1", "根1")
            sourceTree.addNode("mid1", "root1", "中间节点1")
            sourceTree.addNode("leaf1", "mid1", "叶节点1")

            // 只同步叶节点，应该递归同步所有必要的父节点
            synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("leaf1"))

            // 验证所有必要的父节点都被同步
            Assertions.assertNotNull(targetTree.findNodeByKey("root1"))
            Assertions.assertNotNull(targetTree.findNodeByKey("mid1"))
            Assertions.assertNotNull(targetTree.findNodeByKey("leaf1"))
        }

        @Test
        fun `处理不同排序基数的树`() {
            // 创建不同排序基数的树
            val sourceTreeDifferentBase = DefaultSortingMultipleTree<String, String>("ROOT", 1000L)
            val targetTreeDifferentBase = DefaultSortingMultipleTree<String, String>("ROOT", 100L)

            // 在源树中添加节点
            sourceTreeDifferentBase.addRootNode("root1", "根", 1L)
            sourceTreeDifferentBase.addNode("child1", "root1", "子", 1L)

            // 同步
            synchronizer.synchronizeTrees(sourceTreeDifferentBase, targetTreeDifferentBase)

            // 验证目标树中节点的排序值被正确转换
            val targetRoot = targetTreeDifferentBase.findNodeByKey("root1")
            Assertions.assertNotNull(targetRoot)
            Assertions.assertEquals(1L, targetRoot!!.sort)

            val targetChild = targetTreeDifferentBase.findNodeByKey("child1")
            Assertions.assertNotNull(targetChild)
            // 子节点的排序值在目标树中应基于目标树的基数重新计算，但其相对顺序（sort）应保持不变
            Assertions.assertEquals(1L, targetChild!!.sort % targetTreeDifferentBase.sortBase)
        }

        @Test
        fun `选择性同步一个源树中不存在的节点`() {
            sourceTree.addRootNode("root1", "根节点1")

            // 选择性同步一个不存在的key, 抛出异常
            assertThrows<IllegalArgumentException> {
                synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("nonExistentNode"))
            }
        }

        @Test
        fun `同步时替换节点（删除旧节点添加同key新节点）`() {
            // 初始结构
            sourceTree.addRootNode("node1", "节点v1")
            synchronizer.synchronizeTrees(sourceTree, targetTree)
            Assertions.assertEquals("节点v1", targetTree.findNodeByKey("node1")?.data)

            // 在源树中"替换"节点
            sourceTree.removeNode("node1")
            sourceTree.addRootNode("node1", "节点v2") // 重新添加同key节点
            synchronizer.synchronizeTrees(sourceTree, targetTree)

            // 验证目标树
            val node = targetTree.findNodeByKey("node1")
            Assertions.assertNotNull(node)
            Assertions.assertEquals("节点v2", node?.data)
            Assertions.assertTrue(node!!.children.isEmpty()) // 确保是新节点，没有旧的子节点
        }
    }
}
