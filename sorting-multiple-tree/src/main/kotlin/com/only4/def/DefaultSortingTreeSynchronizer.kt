package com.only4.def

import com.only4.SortingMultipleTree
import com.only4.SortingTreeNode
import com.only4.SortingTreeSynchronizer
import com.only4.SortingTreeSynchronizer.SyncResult
import com.only4.SortingTreeSynchronizer.SyncType

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
    ): List<SyncResult<K, V>> {
        val sourceNodeMap = sourceTree.flattenTree().associateBy { it.key }
        val targetNodeMap = targetTree.flattenTree().associateBy { it.key }
        return calculateDifferences(sourceTree, targetTree, sourceNodeMap, targetNodeMap)
    }

    override fun calculateDifferences(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        sourceNodeMap: Map<K, SortingTreeNode<K, V>>,
        targetNodeMap: Map<K, SortingTreeNode<K, V>>
    ): List<SyncResult<K, V>> {
        val allKeys = sourceNodeMap.keys + targetNodeMap.keys
        return allKeys.mapNotNull { key ->
            val sourceNode = sourceNodeMap[key]
            val targetNode = targetNodeMap[key]

            when {
                sourceNode != null && targetNode == null -> SyncResult(sourceNode, SyncType.ADD)
                sourceNode == null && targetNode != null -> SyncResult(targetNode, SyncType.DELETE)
                sourceNode != null && targetNode != null -> {
                    if (compareNodesFully(sourceNode, targetNode, sourceTree, targetTree)) {
                        SyncResult(sourceNode, SyncType.SAME)
                    } else {
                        SyncResult(sourceNode, SyncType.UPDATE)
                    }
                }

                else -> null
            }
        }
    }

    /**
     * 比较两个节点是否键相同
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
        // 比较相对排序索引，而不是绝对排序值
        return getSortIndex(node1.sort, sourceTree.sortBase) == getSortIndex(node2.sort, targetTree.sortBase)
    }

    /**
     * 比较两个节点是否完全相同
     */
    private fun compareNodesFully(
        node1: SortingTreeNode<K, V>,
        node2: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): Boolean {
        return compareNodesByKey(node1, node2) &&
                node1.parentKey == node2.parentKey &&
                compareNodesBySort(node1, node2, sourceTree, targetTree) &&
                dataComparator(node1.data, node2.data)
    }

    /**
     * 同步两棵树
     */
    override fun synchronizeTrees(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        keys: Collection<K>
    ): List<SyncResult<K, V>> {
        val sourceNodeMap = sourceTree.flattenTree().associateBy { it.key }
        val targetNodeMap = targetTree.flattenTree().associateBy { it.key }

        // 如果提供了keys，验证它们至少存在于一棵树中
        if (keys.isNotEmpty()) {
            val validKeys = keys.filter { it in sourceNodeMap || it in targetNodeMap }
            if (validKeys.isEmpty()) {
                throw IllegalArgumentException("None of the specified keys exist in either tree: $keys")
            }
        }

        val differences = calculateDifferences(sourceTree, targetTree, sourceNodeMap, targetNodeMap)

        val resultsToSync = if (keys.isEmpty()) {
            differences
        } else {
            val syncResultMap = differences.associateBy { it.node.key }
            keys.mapNotNull { key -> syncResultMap[key] }
        }

        return synchronizeTreesByResults(sourceTree, targetTree, resultsToSync)
    }

    /**
     * 同步两棵树，根据指定的同步结果列表
     */
    override fun synchronizeTreesByResults(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        syncResults: Collection<SyncResult<K, V>>
    ): List<SyncResult<K, V>> {
        if (syncResults.isEmpty()) return emptyList()

        val sourceNodeMap = sourceTree.flattenTree().associateBy { it.key }

        // 验证 syncResults 至少有一个节点存在于源树或目标树中
        val allTargetKeys = lazy { targetTree.flattenTree().map { it.key }.toSet() }
        if (syncResults.none { it.node.key in sourceNodeMap || it.node.key in allTargetKeys.value }) {
            throw IllegalArgumentException("None of the nodes in syncResults exist in either tree.")
        }

        val nodeMapByKey = mutableMapOf<K, SortingTreeNode<K, V>>()
        val processedResults = mutableListOf<SyncResult<K, V>>()

        val differencesByType = syncResults.groupBy { it.syncType }

        // 1. 处理添加
        differencesByType[SyncType.ADD]?.forEach { result ->
            processAddNode(result.node, sourceTree, targetTree, nodeMapByKey).also { processedResults.add(it) }
        }

        // 2. 处理更新
        differencesByType[SyncType.UPDATE]?.forEach { result ->
            processUpdateNode(result.node, sourceTree, targetTree, nodeMapByKey).also { processedResults.add(it) }
        }

        // 3. 处理删除
        differencesByType[SyncType.DELETE]?.forEach { result ->
            processDeleteNode(result.node, targetTree).also { processedResults.add(it) }
        }

        // 4. 处理相同节点（仅用于填充映射，以便其他操作可以找到它们）
        differencesByType[SyncType.SAME]?.forEach { result ->
            nodeMapByKey[result.node.key] = result.node
            processedResults.add(result)
        }

        return processedResults
    }

    /**
     * 递归处理节点添加操作，确保父节点先被添加
     */
    private fun processAddNode(
        node: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>
    ): SyncResult<K, V> {
        // 如果节点已存在（可能由其他操作的父节点递归添加），则直接返回
        if (targetTree.findNodeByKey(node.key) != null) {
            val existingNode = targetTree.findNodeByKey(node.key)!!
            return SyncResult(existingNode, SyncType.ADD, true)
        }

        // 确保父节点存在
        if (!sourceTree.isRoot(node) && targetTree.findNodeByKey(node.parentKey) == null) {
            sourceTree.findNodeByKey(node.parentKey)?.let { parent ->
                processAddNode(parent, sourceTree, targetTree, nodeMapByKey)
            }
        }

        val sortIndex = getSortIndex(node.sort, sourceTree.sortBase)
        val addedNode = if (sourceTree.isRoot(node)) {
            targetTree.addRootNode(node.key, node.data, sortIndex)
        } else {
            targetTree.addNode(node.key, node.parentKey, node.data, sortIndex)
        }

        nodeMapByKey[addedNode.key] = addedNode
        return SyncResult(addedNode, SyncType.ADD, true)
    }

    /**
     * 处理节点更新操作，统一处理移动、排序和数据变更
     */
    private fun processUpdateNode(
        sourceNode: SortingTreeNode<K, V>,
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        nodeMapByKey: MutableMap<K, SortingTreeNode<K, V>>
    ): SyncResult<K, V> {
        val targetNode = targetTree.findNodeByKey(sourceNode.key)
            ?: return SyncResult(sourceNode, SyncType.UPDATE, false)

        val sourceSortIndex = getSortIndex(sourceNode.sort, sourceTree.sortBase)
        val targetSortIndex = getSortIndex(targetNode.sort, targetTree.sortBase)

        val parentChanged = targetNode.parentKey != sourceNode.parentKey
        val sortChanged = sourceSortIndex != targetSortIndex
        val dataChanged = !dataComparator(targetNode.data, sourceNode.data)

        if (!parentChanged && !sortChanged && !dataChanged) {
            return SyncResult(targetNode, SyncType.UPDATE, false)
        }

        var finalNode = targetNode
        var modified = false

        // 处理移动和排序变更
        if (parentChanged || sortChanged) {
            // 确保新的父节点存在
            if (parentChanged && !sourceTree.isRoot(sourceNode) && targetTree.findNodeByKey(sourceNode.parentKey) == null) {
                sourceTree.findNodeByKey(sourceNode.parentKey)?.let { sourceParent ->
                    processAddNode(sourceParent, sourceTree, targetTree, nodeMapByKey)
                }
            }

            val movedNode = if (sourceTree.isRoot(sourceNode)) {
                targetTree.moveNodeToRoot(targetNode, sourceSortIndex)
            } else {
                targetTree.moveNode(targetNode, sourceNode.parentKey, sourceSortIndex)
            }

            finalNode = movedNode
            modified = true
        }

        // 处理数据变更
        if (dataChanged) {
            finalNode.data = sourceNode.data
            modified = true
        }

        nodeMapByKey[finalNode.key] = finalNode

        return SyncResult(finalNode, SyncType.UPDATE, modified)
    }

    /**
     * 处理节点删除操作
     */
    private fun processDeleteNode(
        node: SortingTreeNode<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): SyncResult<K, V> {
        val success = targetTree.removeNode(node.key)
        return SyncResult(node, SyncType.DELETE, success)
    }

    /**
     * 获取节点在其父节点下的相对排序索引
     */
    private fun getSortIndex(sort: Long, sortBase: Long): Long {
        return sort % sortBase
    }
}
