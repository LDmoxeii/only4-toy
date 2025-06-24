package com.only4.algorithm.structure.def

import com.only4.algorithm.structure.SortingMultipleTree
import com.only4.algorithm.structure.SortingTreeNode
import com.only4.algorithm.structure.SortingTreeSynchronizer

/**
 * 有序多叉树同步器默认实现
 *
 * @param K 节点键类型
 * @param V 节点数据类型
 * @param dataComparator 用于比较节点数据的比较器，默认使用equals方法
 */
class DefaultSortingTreeSynchronizer<K, V>(
    private val dataComparator: (V, V) -> Boolean = { v1, v2 -> v1 == v2 }
) : SortingTreeSynchronizer<K, V> {

    /**
     * 统计两棵树之间的差异
     */
    override fun calculateDifferences(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): List<SortingTreeSynchronizer.SyncResult<K, V>> {
        val result = mutableListOf<SortingTreeSynchronizer.SyncResult<K, V>>()

        // 获取所有节点
        val sourceNodes = sourceTree.flattenTree()
        val targetNodes = targetTree.flattenTree()

        // 创建目标树节点的键映射，用于快速查找
        val targetNodeMap = targetNodes.associateBy { it.key }

        // 处理源树中的每个节点
        for (sourceNode in sourceNodes) {
            val targetNode = targetNodeMap[sourceNode.key]

            if (targetNode == null) {
                // 情况2：源树有，目标树没有 -> 需要新增
                result.add(SortingTreeSynchronizer.SyncResult(sourceNode, SortingTreeSynchronizer.SyncType.ADD))
            } else {
                // 检查节点是否完全相同
                if (compareNodesFully(sourceNode, targetNode, sourceTree, targetTree)) {
                    // 情况1：两个节点完全相同
                    result.add(SortingTreeSynchronizer.SyncResult(sourceNode, SortingTreeSynchronizer.SyncType.SAME))
                } else {
                    // 情况4：键相同但其他属性不同 -> 需要更新
                    result.add(SortingTreeSynchronizer.SyncResult(sourceNode, SortingTreeSynchronizer.SyncType.UPDATE))
                }
            }
        }

        // 创建源树节点的键映射，用于快速查找
        val sourceNodeMap = sourceNodes.associateBy { it.key }

        // 处理目标树中源树没有的节点（需要删除的节点）
        for (targetNode in targetNodes) {
            if (!sourceNodeMap.containsKey(targetNode.key)) {
                // 情况3：源树没有，目标树有 -> 需要删除
                result.add(SortingTreeSynchronizer.SyncResult(targetNode, SortingTreeSynchronizer.SyncType.DELETE))
            }
        }

        return result
    }

    /**
     * 比较两个节点是否键相同
     * 粒度1：仅比较唯一键
     */
    private fun compareNodesByKey(node1: SortingTreeNode<K, V>, node2: SortingTreeNode<K, V>): Boolean {
        return node1.key == node2.key
    }

    private fun compareNodesBySort(
        node1: SortingTreeNode<K, V>,
        node2: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): Boolean {
        val sourceSortBase = sourceTree.sortBase
        val targetSortBase = targetTree.sortBase
        return (node1.sort / sourceSortBase) == (node2.sort / targetSortBase) &&
                (node1.sort % sourceSortBase) == (node2.sort % targetSortBase)
    }

    /**
     * 比较两个节点是否完全相同
     * 粒度2：比较父节点唯一键、节点排序值和节点数据
     */
    private fun compareNodesFully(
        node1: SortingTreeNode<K, V>,
        node2: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): Boolean {
        return this.compareNodesByKey(node1, node2) &&
                node1.parentKey == node2.parentKey &&
                compareNodesBySort(node1, node2, sourceTree, targetTree) &&
                dataComparator(node1.data, node2.data)
    }

    /**
     * 同步两棵树
     *
     * @param sourceTree 源树（作为同步的基准）
     * @param targetTree 目标树（将被修改以匹配源树）
     * @param keys 需要同步的节点键列表，如果为空则同步所有节点
     * @return 同步后的差异结果列表
     */
    override fun synchronizeTrees(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        keys: Collection<K>
    ): List<SortingTreeSynchronizer.SyncResult<K, V>> {
        // 计算差异
        val differences = calculateDifferences(sourceTree, targetTree)

        // 如果keys为空，同步所有差异
        if (keys.isEmpty()) {
            return synchronizeTreesByResults(sourceTree, targetTree, differences)
        }

        // 创建键到同步结果的映射
        val syncResultMap = differences.associateBy { it.node.key }

        // 找出需要同步的结果
        val syncResults = mutableListOf<SortingTreeSynchronizer.SyncResult<K, V>>()

        // 添加显式要求同步的节点
        for (key in keys) {
            syncResultMap[key]?.let { syncResults.add(it) }
        }

        return synchronizeTreesByResults(sourceTree, targetTree, syncResults)
    }

    /**
     * 同步两棵树，根据指定的同步结果列表
     *
     * @param sourceTree 源树（作为同步的基准）
     * @param targetTree 目标树（将被修改以匹配源树）
     * @param syncResults 需要同步的结果列表，如果为空则同步所有差异
     * @return 同步后的差异结果列表
     */
    override fun synchronizeTreesByResults(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        syncResults: Collection<SortingTreeSynchronizer.SyncResult<K, V>>
    ): List<SortingTreeSynchronizer.SyncResult<K, V>> {
        // 如果syncResults为空，计算所有差异并同步
        val differences = if (syncResults.isEmpty()) {
            calculateDifferences(sourceTree, targetTree)
        } else {
            syncResults.toList()
        }

        // 处理顺序：先添加，再更新，最后删除
        val nodeMapByKey = mutableMapOf<K, SortingTreeNode<K, V>>()
        val processedResults = mutableListOf<SortingTreeSynchronizer.SyncResult<K, V>>()

        // 先处理添加操作
        processSyncTypeInOrder(
            differences,
            sourceTree,
            targetTree,
            listOf(SortingTreeSynchronizer.SyncType.ADD),
            nodeMapByKey,
            processedResults
        )

        // 再处理更新操作
        processSyncTypeInOrder(
            differences,
            sourceTree,
            targetTree,
            listOf(SortingTreeSynchronizer.SyncType.UPDATE),
            nodeMapByKey,
            processedResults
        )

        // 最后处理删除操作
        processSyncTypeInOrder(
            differences,
            sourceTree,
            targetTree,
            listOf(SortingTreeSynchronizer.SyncType.DELETE),
            nodeMapByKey,
            processedResults
        )

        return processedResults
    }

    /**
     * 按顺序处理特定类型的同步操作
     */
    private fun processSyncTypeInOrder(
        differences: Collection<SortingTreeSynchronizer.SyncResult<K, V>>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        syncTypes: List<SortingTreeSynchronizer.SyncType>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>,
        processedResults: MutableList<SortingTreeSynchronizer.SyncResult<K, V>>
    ) {
        // 筛选出指定类型的同步结果
        val filteredResults = differences.filter { it.syncType in syncTypes }

        // 处理每个同步结果
        for (result in filteredResults) {
            val processed = when (result.syncType) {
                SortingTreeSynchronizer.SyncType.ADD -> processAddNode(
                    result.node,
                    sourceTree,
                    targetTree,
                    nodeMapByKey
                )

                SortingTreeSynchronizer.SyncType.UPDATE -> processUpdateNode(
                    result.node,
                    sourceTree,
                    targetTree,
                    nodeMapByKey
                )

                SortingTreeSynchronizer.SyncType.DELETE -> processDeleteNode(result.node, targetTree, nodeMapByKey)
                SortingTreeSynchronizer.SyncType.SAME -> {
                    // 对于相同节点，不需要进行任何操作
                    nodeMapByKey[result.node.key] = result.node
                    result
                }
            }

            processedResults.add(processed)
        }
    }

    /**
     * 处理节点添加操作
     */
    private fun processAddNode(
        node: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>
    ): SortingTreeSynchronizer.SyncResult<K, V> {
        // 获取目标树中的父节点
        var targetParentNode = targetTree.findNodeByKey(node.parentKey)

        // 如果父节点不存在且不是根节点的虚拟父键
        if (targetParentNode == null && !sourceTree.isRoot(node)) {
            // 查找源树中的父节点
            val sourceParentNode = sourceTree.findNodeByKey(node.parentKey)

            // 如果源树中存在父节点，先确保父节点被同步
            if (sourceParentNode != null) {
                // 递归同步父节点
                val parentResult = processAddNode(sourceParentNode, sourceTree, targetTree, nodeMapByKey)

                // 重新检查目标树中是否已经有了父节点
                targetParentNode = targetTree.findNodeByKey(node.parentKey)
            }
        }

        // 添加节点到目标树
        val addedNode = if (sourceTree.isRoot(node)) {
            targetTree.addRootNode(
                node.key,
                node.data,
                getSortIndex(node.sort, sourceTree.sortBase)
            )
        } else {
            targetTree.addNode(
                node.key,
                node.parentKey,
                node.data,
                getSortIndex(node.sort, sourceTree.sortBase)
            )
        }

        // 将添加的节点放入映射中，以便后续操作使用
        nodeMapByKey[node.key] = addedNode

        return SortingTreeSynchronizer.SyncResult(addedNode, SortingTreeSynchronizer.SyncType.ADD, true)
    }

    /**
     * 处理节点更新操作
     */
    private fun processUpdateNode(
        node: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>
    ): SortingTreeSynchronizer.SyncResult<K, V> {
        // 获取目标树中的节点
        val targetNode = targetTree.findNodeByKey(node.key) ?: return SortingTreeSynchronizer.SyncResult(
            node,
            SortingTreeSynchronizer.SyncType.UPDATE,
            false
        )

        // 检查父节点是否需要更改
        if (targetNode.parentKey != node.parentKey || (!(sourceTree.isRoot(node)) && !(targetTree.isRoot(targetNode)))) {
            // 确保新的父节点存在
            var targetParentNode = targetTree.findNodeByKey(node.parentKey)

            // 如果新的父节点不存在且不是根节点的虚拟父键
            if (targetParentNode == null && sourceTree.isRoot(node)) {
                // 查找源树中的父节点
                val sourceParentNode = sourceTree.findNodeByKey(node.parentKey)

                // 如果源树中存在父节点，先确保父节点被同步
                if (sourceParentNode != null) {
                    // 递归同步父节点
                    val parentResult = processAddNode(sourceParentNode, sourceTree, targetTree, nodeMapByKey)
                }
            }

            // 移动节点到新的父节点下
            val movedNode = if (sourceTree.isRoot(node)) {
                targetTree.moveNodeToRoot(
                    targetNode,
                    getSortIndex(node.sort, sourceTree.sortBase)
                )
            } else {
                targetTree.moveNode(
                    targetNode,
                    node.parentKey,
                    getSortIndex(node.sort, sourceTree.sortBase)
                )
            }

            // 更新映射
            nodeMapByKey[node.key] = movedNode
            return SortingTreeSynchronizer.SyncResult(movedNode, SortingTreeSynchronizer.SyncType.UPDATE, true)
        } else if (getSortIndex(targetNode.sort, targetTree.sortBase) != getSortIndex(node.sort, sourceTree.sortBase)) {
            // 如果只有排序值需要更改，移动节点但保持相同的父节点
            val movedNode = targetTree.moveNode(
                targetNode.key,
                targetNode.parentKey,
                getSortIndex(node.sort, sourceTree.sortBase)
            )

            // 更新映射
            if (movedNode != null) {
                nodeMapByKey[node.key] = movedNode
                return SortingTreeSynchronizer.SyncResult(movedNode, SortingTreeSynchronizer.SyncType.UPDATE, true)
            }
        }

        // 如果没有移动成功或者不需要移动，返回更新失败的结果
        return SortingTreeSynchronizer.SyncResult(targetNode, SortingTreeSynchronizer.SyncType.UPDATE, false)
    }

    /**
     * 处理节点删除操作
     */
    private fun processDeleteNode(
        node: SortingTreeNode<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>
    ): SortingTreeSynchronizer.SyncResult<K, V> {
        // 从目标树中删除节点
        val success = targetTree.removeNode(node.key)

        // 如果删除成功，从映射中移除该节点
        if (success) {
            nodeMapByKey.remove(node.key)
        }

        return SortingTreeSynchronizer.SyncResult(node, SortingTreeSynchronizer.SyncType.DELETE, success)
    }

    /**
     * 获取节点排序索引
     */
    private fun getSortIndex(sort: Long, sortBase: Long): Long {
        return sort % sortBase
    }
}
