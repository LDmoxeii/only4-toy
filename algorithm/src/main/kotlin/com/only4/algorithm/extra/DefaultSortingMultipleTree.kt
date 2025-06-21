package com.only4.algorithm.extra

/**
 * 有序多叉树实现类
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
class DefaultSortingMultipleTree<K, V>(
    // 根节点的虚拟父键标识
    private val ROOT_KEY: K? = null,
    // 路径分隔符
    private val PATH_SEPARATOR: String = "/",
    // 节点排序基数
    private val SORT_BASE: Long = 100L,
) : AbstractOrderedMultipleTree<K, V>(), SortingMultipleTree<K, V> {

    /**
     * 添加节点（自动分配排序值）
     */
    override fun addNode(key: K, parentKey: K?, data: V): OrderedTreeNode<K, V> {
        // 检查键是否已存在
        if (nodeMap.containsKey(key)) {
            throw IllegalArgumentException("Node with key $key already exists")
        }

        // 计算下一个子节点索引
        val nextSortIndex = calculateNextSortIndex(parentKey)

        // 使用子节点索引添加节点
        return addNode(key, parentKey, data, nextSortIndex)
    }

    /**
     * 添加节点（指定排序值）
     */
    override fun addNode(key: K, parentKey: K?, data: V, sort: Long): OrderedTreeNode<K, V> {
        // 检查键是否已存在
        if (nodeMap.containsKey(key)) {
            throw IllegalArgumentException("Node with key $key already exists")
        }

        // 获取下一个可用的排序索引
        val nextAvailableIndex = calculateNextSortIndex(parentKey)

        // 如果指定的排序号大于下一个可用的排序索引，则使用下一个可用的排序索引
        val actualSortIndex = if (sort > nextAvailableIndex) nextAvailableIndex else sort

        // 获取父节点排序值
        val parentSort = if (parentKey != null) findNodeByKey(parentKey)?.sort ?: 0L else 0L
        // 计算实际排序值: 父节点排序 * 100 + 实际的排序索引
        val actualSort = parentSort * SORT_BASE + actualSortIndex

        // 生成节点路径
        val nodePath = generateNodePath(key, parentKey)

        // 创建节点
        val node = DefaultOrderedTreeNode(key, parentKey, actualSort, nodePath, data)

        // 检查是否需要解决排序冲突
        handleSortConflict(parentKey, actualSort)

        // 将节点添加到各种映射中
        nodeMap[key] = node

        // 添加到父子映射
        parentChildMap.getOrPut(parentKey) { mutableListOf() }.add(node)

        // 添加到路径映射
        pathNodeMap.getOrPut(nodePath) { mutableListOf() }.add(node)

        // 如果父节点存在，将子节点添加到父节点的子节点列表
        if (parentKey != null) {
            findNodeByKey(parentKey)?.children?.add(node)
        }

        return node
    }

    /**
     * 删除节点
     */
    override fun removeNode(key: K): Boolean {
        // 查找节点
        val node = findNodeByKey(key) ?: return false

        // 递归删除节点及其所有子孙节点
        recursiveRemove(node)

        // 重新排序被删除节点的兄弟节点
        val parentKey = node.parentKey
        val lastSortIndex = getSortIndex(node.sort)

        // 将同级节点向前移动
        moveChildrenForward(parentKey, lastSortIndex)

        return true
    }

    /**
     * 获取树的所有节点列表
     */
    override fun getAllNodes(): List<OrderedTreeNode<K, V>> {
        return nodeMap.values.toList().sortedBy { it.sort }
    }

    /**
     * 获取树的根节点列表
     */
    override fun getRootNodes(): List<OrderedTreeNode<K, V>> {
        return findNodesByParentKey(ROOT_KEY)
    }

    /**
     * 将树转换为扁平列表
     */
    override fun flattenTree(): List<OrderedTreeNode<K, V>> {
        return nodeMap.values.sortedBy { it.sort }
    }

    /**
     * 生成节点路径
     */
    override fun generateNodePath(key: K, parentKey: K?): String {
        if (parentKey == null) {
            return "$key"
        }

        val parentNode = findNodeByKey(parentKey)
        return if (parentNode != null) {
            "${parentNode.nodePath}$PATH_SEPARATOR$key"
        } else {
            "$key"
        }
    }

    /**
     * 计算下一个排序值
     */
    override fun calculateNextSort(parentKey: K?): Int {
        return calculateNextSortIndex(parentKey).toInt()
    }

    /**
     * 处理排序冲突
     */
    private fun handleSortConflict(parentKey: K?, newSort: Long) {
        val sortIndex = getSortIndex(newSort)
        val children = parentChildMap[parentKey] ?: return

        val conflictNodes = children.filter { getSortIndex(it.sort) >= sortIndex }
        if (conflictNodes.isNotEmpty()) {
            // 将冲突的节点排序值递增
            for (node in conflictNodes.sortedBy { it.sort }) {
                if (node is DefaultOrderedTreeNode) {
                    val oldSort = node.sort
                    val newSortIndex = getSortIndex(oldSort) + 1
                    val parentSort = getParentSort(oldSort)
                    node.sort = parentSort * SORT_BASE + newSortIndex

                    // 递归更新子节点排序
                    updateChildrenSort(node.key, oldSort, node.sort)
                }
            }
        }
    }

    /**
     * 重新排序子节点，解决排序冲突
     */
    override fun reorderChildren(parentKey: K?, startSort: Int) {
        reorderChildren(parentKey, startSort.toLong())
    }

    /**
     * 重新排序子节点，解决排序冲突（Long版本）
     */
    private fun reorderChildren(parentKey: K?, startSort: Long) {
        val children = parentChildMap[parentKey]?.sortedBy { it.sort } ?: return
        val startIndex = getSortIndex(startSort).toInt()

        // 只处理从startSort开始的节点
        val nodesToReorder = children.filter { getSortIndex(it.sort) >= startIndex }

        val parentSort = if (children.isNotEmpty()) getParentSort(children[0].sort) else 0L

        for ((index, node) in nodesToReorder.withIndex()) {
            if (node is DefaultOrderedTreeNode) {
                val oldSort = node.sort
                val newSort = parentSort * SORT_BASE + (startIndex + index)

                // 如果排序值需要变更，则更新
                if (oldSort != newSort) {
                    node.sort = newSort

                    // 递归更新所有子节点的排序
                    updateChildrenSort(node.key, oldSort, newSort)
                }
            }
        }
    }

    /**
     * 移动子节点排序（当删除节点时）
     */
    override fun moveChildrenForward(parentKey: K?, removedSort: Int) {
        moveChildrenForward(parentKey, removedSort.toLong())
    }

    /**
     * 移动子节点排序（当删除节点时）（Long版本）
     */
    private fun moveChildrenForward(parentKey: K?, removedSortIndex: Long) {
        val children = parentChildMap[parentKey]?.sortedBy { it.sort } ?: return

        // 只处理需要前移的节点（排序值大于被删除节点的排序值）
        val nodesToMove = children.filter { getSortIndex(it.sort) > removedSortIndex }

        for (node in nodesToMove) {
            if (node is DefaultOrderedTreeNode) {
                val oldSort = node.sort
                val newSortIndex = getSortIndex(oldSort) - 1
                val parentSort = getParentSort(oldSort)
                val newSort = parentSort * SORT_BASE + newSortIndex

                node.sort = newSort

                // 递归更新所有子节点的排序
                updateChildrenSort(node.key, oldSort, newSort)
            }
        }
    }

    /**
     * 递归删除节点及其子节点
     */
    override fun recursiveRemove(node: OrderedTreeNode<K, V>) {
        // 首先递归删除所有子节点
        val childrenCopy = node.children.toList()
        for (child in childrenCopy) {
            recursiveRemove(child)
        }

        // 删除当前节点
        nodeMap.remove(node.key)

        // 从父子映射中删除
        val parentKey = node.parentKey
        parentChildMap[parentKey]?.remove(node)
        if (parentChildMap[parentKey]?.isEmpty() == true) {
            parentChildMap.remove(parentKey)
        }

        // 从路径映射中删除
        pathNodeMap[node.nodePath]?.remove(node)
        if (pathNodeMap[node.nodePath]?.isEmpty() == true) {
            pathNodeMap.remove(node.nodePath)
        }

        // 从父节点的子列表中删除
        val nodeParentKey = node.parentKey
        if (nodeParentKey != null) {
            val parentNode = findNodeByKey(nodeParentKey)
            parentNode?.children?.remove(node)
        }
    }

    /**
     * 计算下一个排序索引
     */
    private fun calculateNextSortIndex(parentKey: K?): Long {
        val children = parentChildMap[parentKey] ?: return 1L

        return if (children.isEmpty()) {
            1L
        } else {
            // 找出最大的排序索引并加1
            children.maxOfOrNull { getSortIndex(it.sort) }?.plus(1) ?: 1L
        }
    }

    /**
     * 获取排序索引（排序值的最后两位）
     */
    private fun getSortIndex(sort: Long): Long {
        return sort % SORT_BASE
    }

    /**
     * 获取父节点排序值
     */
    private fun getParentSort(sort: Long): Long {
        return sort / SORT_BASE
    }

    /**
     * 递归更新子节点的排序
     */
    private fun updateChildrenSort(parentKey: K, oldParentSort: Long, newParentSort: Long) {
        val children = parentChildMap[parentKey] ?: return

        for (child in children) {
            if (child is DefaultOrderedTreeNode) {
                val childSortIndex = getSortIndex(child.sort)
                val oldSort = child.sort
                val newSort = newParentSort * SORT_BASE + childSortIndex

                child.sort = newSort

                // 递归更新更深层的子节点
                updateChildrenSort(child.key, oldSort, newSort)
            }
        }
    }
}
