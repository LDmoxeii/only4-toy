package com.only4.algorithm.extra

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * 有序多叉树同步器测试类
 */
class SortingTreeSynchronizerTest {

    // 测试用的树实例
    private lateinit var sourceTree: DefaultSortingMultipleTree<String, String>
    private lateinit var targetTree: DefaultSortingMultipleTree<String, String>

    // 同步器实例
    private lateinit var synchronizer: SortingTreeSynchronizer<String, String>

    // 根节点的虚拟父键
    private val dummyKey = "root"

    @BeforeEach
    fun setUp() {
        // 初始化树
        sourceTree = DefaultSortingMultipleTree(dummyKey)
        targetTree = DefaultSortingMultipleTree(dummyKey)

        // 初始化同步器
        synchronizer = DefaultOrderedTreeSynchronizer()

        // 构建源树
        sourceTree.apply {
            // 添加一级节点
            addNode("A", dummyKey, "Node A")
            addNode("B", dummyKey, "Node B")
            addNode("C", dummyKey, "Node C")

            // 添加二级节点
            addNode("A1", "A", "Node A1")
            addNode("A2", "A", "Node A2")
            addNode("B1", "B", "Node B1")
            addNode("C1", "C", "Node C1")

            // 添加三级节点
            addNode("A11", "A1", "Node A11")
            addNode("A12", "A1", "Node A12")
        }

        // 构建目标树（与源树有一些差异）
        targetTree.apply {
            // 添加一级节点（B节点数据不同，缺少C节点）
            addNode("A", dummyKey, "Node A")
            addNode("B", dummyKey, "Node B - Different")
            addNode("D", dummyKey, "Node D") // 源树中不存在

            // 添加二级节点（A2位置不同，缺少C1节点）
            addNode("A1", "A", "Node A1")
            addNode("A2", "B", "Node A2") // 父节点不同
            addNode("B1", "B", "Node B1")

            // 添加三级节点（A11数据不同，缺少A12节点）
            addNode("A11", "A1", "Node A11 - Different")
        }
    }

    @Test
    fun `测试比较节点键`() {
        val nodeA1Source = sourceTree.findNodeByKey("A1")!!
        val nodeA1Target = targetTree.findNodeByKey("A1")!!

        assertTrue(synchronizer.compareNodesByKey(nodeA1Source, nodeA1Target))

        val nodeA = sourceTree.findNodeByKey("A")!!
        val nodeB = sourceTree.findNodeByKey("B")!!

        assertFalse(synchronizer.compareNodesByKey(nodeA, nodeB))
    }

    @Test
    fun `测试完全比较节点`() {
        // 相同节点
        val nodeA1Source = sourceTree.findNodeByKey("A1")!!
        val nodeA1Target = targetTree.findNodeByKey("A1")!!
        assertTrue(synchronizer.compareNodesFully(nodeA1Source, nodeA1Target))

        // 数据不同
        val nodeB = sourceTree.findNodeByKey("B")!!
        val nodeBTarget = targetTree.findNodeByKey("B")!!
        assertFalse(synchronizer.compareNodesFully(nodeB, nodeBTarget))

        // 父节点不同
        val nodeA2Source = sourceTree.findNodeByKey("A2")!!
        val nodeA2Target = targetTree.findNodeByKey("A2")!!
        assertFalse(synchronizer.compareNodesFully(nodeA2Source, nodeA2Target))
    }

    @Test
    fun `测试计算差异`() {
        val differences = synchronizer.calculateDifferences(sourceTree, targetTree)

        // 验证差异数量
        assertEquals(9, differences.size)

        // 验证各类型差异的数量
        val sameCount = differences.count { it.syncType == SortingTreeSynchronizer.SyncType.SAME }
        val addCount = differences.count { it.syncType == SortingTreeSynchronizer.SyncType.ADD }
        val deleteCount = differences.count { it.syncType == SortingTreeSynchronizer.SyncType.DELETE }
        val updateCount = differences.count { it.syncType == SortingTreeSynchronizer.SyncType.UPDATE }

        assertEquals(3, sameCount) // A, A1, B1 相同
        assertEquals(3, addCount)  // C, C1, A12 需要添加
        assertEquals(1, deleteCount) // D 需要删除
        assertEquals(2, updateCount) // B 数据不同, A2 父节点不同
    }

    @Test
    fun `测试同步树`() {
        // 执行同步
        val syncResults = synchronizer.synchronizeTrees(sourceTree, targetTree)

        // 验证同步后的节点数量
        assertEquals(sourceTree.flattenTree().size, targetTree.flattenTree().size)

        // 验证特定节点是否正确同步

        // 1. 验证节点B的数据是否更新
        val nodeBTarget = targetTree.findNodeByKey("B")!!
        assertEquals("Node B", nodeBTarget.data)

        // 2. 验证节点A2的父节点是否更新
        val nodeA2Target = targetTree.findNodeByKey("A2")!!
        assertEquals("A", nodeA2Target.parentKey)

        // 3. 验证节点C是否添加
        val nodeCTarget = targetTree.findNodeByKey("C")
        assertNotNull(nodeCTarget)
        assertEquals("Node C", nodeCTarget!!.data)

        // 4. 验证节点D是否删除
        val nodeDTarget = targetTree.findNodeByKey("D")
        assertNull(nodeDTarget)
    }

    @Test
    fun `测试部分同步`() {
        // 只同步A和B节点
        val syncResults = synchronizer.synchronizeTrees(sourceTree, targetTree, listOf("A", "B"))

        // 验证A2节点的父节点是否更新（A的子节点）
        val nodeA2Target = targetTree.findNodeByKey("A2")!!
        assertEquals("A", nodeA2Target.parentKey)

        // 验证B节点的数据是否更新
        val nodeBTarget = targetTree.findNodeByKey("B")!!
        assertEquals("Node B", nodeBTarget.data)

        // 验证C节点是否未添加（因为不在同步列表中）
        val nodeCTarget = targetTree.findNodeByKey("C")
        assertNull(nodeCTarget)

        // 验证D节点是否未删除（因为不在同步列表中）
        val nodeDTarget = targetTree.findNodeByKey("D")
        assertNotNull(nodeDTarget)
    }
}
