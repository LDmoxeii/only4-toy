package com.only4.algorithm.extra

/**
 * 有序多叉树同步器默认实现
 *
 * @param K 节点键类型
 * @param V 节点数据类型
 * @param dataComparator 用于比较节点数据的比较器，默认使用equals方法
 */
class DefaultOrderedTreeSynchronizer<K, V>(
    private val dataComparator: (V, V) -> Boolean = { v1, v2 -> v1 == v2 }
) : SortingTreeSynchronizer<K, V> {

    /**
     * 比较两个节点是否键相同
     * 粒度1：仅比较唯一键
     */
    override fun compareNodesByKey(node1: OrderedTreeNode<K, V>, node2: OrderedTreeNode<K, V>): Boolean {
        return node1.key == node2.key
    }

    /**
     * 比较两个节点是否完全相同
     * 粒度2：比较父节点唯一键、节点排序值和节点数据
     */
    override fun compareNodesFully(node1: OrderedTreeNode<K, V>, node2: OrderedTreeNode<K, V>): Boolean {
        return this.compareNodesByKey(node1, node2) &&
                node1.parentKey == node2.parentKey &&
                node1.sort == node2.sort &&
                dataComparator(node1.data, node2.data)
    }

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
                if (compareNodesFully(sourceNode, targetNode)) {
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
     * 同步两棵树
     */
    override fun synchronizeTrees(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        keys: Collection<K>
    ): List<SortingTreeSynchronizer.SyncResult<K, V>> {
        val sortBase = if ((sourceTree is DefaultSortingMultipleTree<K, V>)) {
            sourceTree.sortBase
        } else 100L

        // 计算差异
        val differences = calculateDifferences(sourceTree, targetTree)

        // 如果指定了keys，则只同步这些键对应的节点
        val filteredDifferences = if (keys.isNotEmpty()) {
            differences.filter { it.node.key in keys }
        } else {
            differences
        }

        // 按照操作类型分组，并按照特定顺序执行操作：先删除，再更新，最后添加
        val (toDelete, toUpdate, toAdd) = filteredDifferences.partition3 {
            when (it.syncType) {
                SortingTreeSynchronizer.SyncType.DELETE -> Triple(true, false, false)
                SortingTreeSynchronizer.SyncType.UPDATE -> Triple(false, true, false)
                SortingTreeSynchronizer.SyncType.ADD -> Triple(false, false, true)
                else -> Triple(false, false, false) // SAME 类型不需要处理
            }
        }

        // 1. 先处理删除操作
        toDelete.forEach { result ->
            targetTree.removeNode(result.node.key)
        }

        // 2. 处理更新操作
        toUpdate.forEach { result ->
            val sourceNode = result.node
            val targetNode = targetTree.findNodeByKey(sourceNode.key)

            if (targetNode != null) {
                // 如果父节点不同，需要移动节点
                if (targetNode.parentKey != sourceNode.parentKey) {
                    targetTree.moveNode(targetNode.key, sourceNode.parentKey, getSortIndex(sourceNode.sort, sortBase))
                }

                // 更新节点数据（如果树实现支持）
                if (targetNode is DefaultOrderedTreeNode) {
                    updateNodeData(targetNode, sourceNode.data)
                }
            }
        }

        // 3. 处理添加操作
        toAdd.forEach { result ->
            val sourceNode = result.node
            targetTree.addNode(
                sourceNode.key,
                sourceNode.parentKey,
                sourceNode.data,
                getSortIndex(sourceNode.sort, sortBase)
            )
        }

        return filteredDifferences
    }

    /**
     * 获取节点的排序索引
     */
    private fun getSortIndex(sort: Long, sortBase: Long = 100L): Long {
        return sort % sortBase
    }

    /**
     * 更新节点数据
     * 注意：这是一个辅助方法，用于在节点类型为DefaultOrderedTreeNode时更新数据
     */
    @Suppress("UNCHECKED_CAST")
    private fun updateNodeData(targetNode: DefaultOrderedTreeNode<K, V>, newData: V) {
        // 使用反射或其他方式更新数据
        // 这里使用了一个技巧，将DefaultOrderedTreeNode的data字段视为可变的
        val nodeClass = targetNode.javaClass
        try {
            val dataField = nodeClass.getDeclaredField("data")
            dataField.isAccessible = true
            dataField.set(targetNode, newData)
        } catch (e: Exception) {
            // 如果无法通过反射更新，可以考虑其他方式
            // 或者要求SortingMultipleTree接口提供updateNodeData方法
        }
    }

    override fun synchronizeTreesByResults(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        keys: Collection<SortingTreeSynchronizer.SyncResult<K, V>>
    ): List<SortingTreeSynchronizer.SyncResult<K, V>> {
        val sortBase = if ((sourceTree is DefaultSortingMultipleTree<K, V>)) {
            sourceTree.sortBase
        } else 100L

        if (keys.isEmpty()) return emptyList()

        // 按照操作类型分组，并按照特定顺序执行操作：先删除，再更新，最后添加
        val (toDelete, toUpdate, toAdd) = keys.partition3 {
            when (it.syncType) {
                SortingTreeSynchronizer.SyncType.DELETE -> Triple(true, false, false)
                SortingTreeSynchronizer.SyncType.UPDATE -> Triple(false, true, false)
                SortingTreeSynchronizer.SyncType.ADD -> Triple(false, false, true)
                else -> Triple(false, false, false) // SAME 类型不需要处理
            }
        }

        // 1. 先处理删除操作
        toDelete.forEach { result ->
            targetTree.removeNode(result.node.key)
        }

        // 2. 处理更新操作
        toUpdate.forEach { result ->
            val sourceNode = result.node
            val targetNode = targetTree.findNodeByKey(sourceNode.key)

            if (targetNode != null) {
                // 如果父节点不同，需要移动节点
                if (targetNode.parentKey != sourceNode.parentKey) {
                    targetTree.moveNode(targetNode.key, sourceNode.parentKey, getSortIndex(sourceNode.sort, sortBase))
                }

                // 更新节点数据
                if (targetNode is DefaultOrderedTreeNode) {
                    updateNodeData(targetNode, sourceNode.data)
                }
            }
        }

        // 3. 处理添加操作
        toAdd.forEach { result ->
            val sourceNode = result.node
            targetTree.addNode(
                sourceNode.key,
                sourceNode.parentKey,
                sourceNode.data,
                getSortIndex(sourceNode.sort, sortBase)
            )
        }

        return keys.toList()
    }
}

/**
 * 扩展函数：将集合分成三部分
 */
private fun <T> Collection<T>.partition3(predicate: (T) -> Triple<Boolean, Boolean, Boolean>): Triple<List<T>, List<T>, List<T>> {
    val first = mutableListOf<T>()
    val second = mutableListOf<T>()
    val third = mutableListOf<T>()

    for (element in this) {
        val (isFirst, isSecond, isThird) = predicate(element)
        when {
            isFirst -> first.add(element)
            isSecond -> second.add(element)
            isThird -> third.add(element)
        }
    }

    return Triple(first, second, third)
}
