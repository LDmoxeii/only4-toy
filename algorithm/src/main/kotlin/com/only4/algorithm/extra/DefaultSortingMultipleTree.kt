package com.only4.algorithm.extra

/**
 * 有序多叉树实现类
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
class DefaultSortingMultipleTree<K, V>(
    // 根节点的虚拟父键标识
    private val dummyKey: K,
    // 路径分隔符
    val pathSeparator: String = "/",
    // 节点排序基数
    val sortBase: Long = 100L,
) : AbstractSortingMultipleTree<K, V>(), SortingMultipleTree<K, V> {

    /**
     * 添加节点（自动分配排序值）
     */
    override fun addNode(key: K, parentKey: K, data: V): OrderedTreeNode<K, V> {
        // 检查键是否已存在
        require(!nodeMap.containsKey(key)) { "Node with key $key already exists" }

        // 计算下一个子节点索引
        val nextSortIndex = calculateNextSort(parentKey)

        // 使用子节点索引添加节点
        return addNode(key, parentKey, data, nextSortIndex)
    }

    /**
     * 添加节点（指定排序值）
     */
    override fun addNode(key: K, parentKey: K, data: V, sort: Long): OrderedTreeNode<K, V> {
        // 检查键是否已存在
        require(!nodeMap.containsKey(key)) { "Node with key $key already exists" }

        // 获取下一个可用的排序索引
        val nextAvailableIndex = calculateNextSort(parentKey)

        // 如果指定的排序号大于下一个可用的排序索引，则使用下一个可用的排序索引
        val actualSortIndex = if (sort > nextAvailableIndex) nextAvailableIndex else sort

        // 获取父节点排序值
        val parentSort = if (parentKey != dummyKey) findNodeByKey(parentKey)?.sort ?: 0L else 0L
        // 计算实际排序值: 父节点排序 * 100 + 实际的排序索引
        val actualSort = parentSort * sortBase + actualSortIndex

        // 生成节点路径
        val nodePath = generateNodePath(key, parentKey)

        // 创建节点
        val node = DefaultOrderedTreeNode(key, parentKey, actualSort, nodePath, data)

        // 检查是否需要解决排序冲突
        handleSortConflict(parentKey, actualSort)

        nodeMap[key] = node
        pathNodeMap[nodePath] = node
        parentChildMap.getOrPut(parentKey) { mutableListOf() }.add(node)

        findNodeByKey(parentKey)?.children?.add(node)

        return node
    }

    /**
     * 删除节点
     */
    override fun removeNode(key: K): Boolean {
        // 查找节点
        val node = findNodeByKey(key) ?: return true

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
     * 获取树的根节点列表
     */
    override fun getRootNodes(): List<OrderedTreeNode<K, V>> {
        return getChildren(dummyKey)
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
    override fun generateNodePath(key: K, parentKey: K): String {
        if (parentKey == dummyKey) {
            return "$key"
        }

        val parentNode = findNodeByKey(parentKey)

        // 确保父节点存在
        requireNotNull(parentNode) { "Parent node with key $parentKey not found" }

        return "${parentNode.nodePath}$pathSeparator$key"
    }

    /**
     * 计算下一个排序值
     */
    override fun calculateNextSort(parentKey: K): Long {
        val children = parentChildMap[parentKey] ?: return 1L

        return if (children.isEmpty()) {
            1L
        } else {
            // 找出最大的排序索引并加1
            children.maxOfOrNull { getSortIndex(it.sort) }?.plus(1) ?: 1L
        }
    }

    /**
     * 处理排序冲突
     */
    override fun handleSortConflict(parentKey: K, startSort: Long) {
        val sortIndex = getSortIndex(startSort)
        val children = parentChildMap[parentKey] ?: return

        // 只处理从sortIndex开始的节点
        val conflictNodes = children.filter { getSortIndex(it.sort) >= sortIndex }
        val parentSort = if (children.isNotEmpty()) getParentSort(children[0].sort) else 0L

        if (conflictNodes.isNotEmpty()) {
            // 将冲突的节点排序值递增
            for (node in conflictNodes.sortedBy { it.sort }) {
                if (node is DefaultOrderedTreeNode) {
                    val oldSort = node.sort
                    val newSortIndex = getSortIndex(oldSort) + 1
                    node.sort = parentSort * sortBase + newSortIndex

                    // 递归更新子节点排序
                    updateChildrenSort(node.key, node.sort)
                }
            }
        }
    }

    /**
     * 递归更新子节点的排序
     */
    private fun updateChildrenSort(parentKey: K, newParentSort: Long) {
        val children = parentChildMap[parentKey] ?: return

        for (child in children) {
            if (child is DefaultOrderedTreeNode) {
                val childSortIndex = getSortIndex(child.sort)
                val newSort = newParentSort * sortBase + childSortIndex

                child.sort = newSort

                // 递归更新更深层的子节点
                updateChildrenSort(child.key, newSort)
            }
        }
    }


    /**
     * 移动子节点排序（当删除节点时）
     */
    override fun moveChildrenForward(parentKey: K, removedSort: Long) {
        val children = parentChildMap[parentKey]?.sortedBy { it.sort } ?: return

        // 只处理需要前移的节点（排序值大于被删除节点的排序值）
        val nodesToMove = children.filter { getSortIndex(it.sort) > removedSort }

        for (node in nodesToMove) {
            if (node is DefaultOrderedTreeNode) {
                val oldSort = node.sort
                val newSortIndex = getSortIndex(oldSort) - 1
                val parentSort = getParentSort(oldSort)
                val newSort = parentSort * sortBase + newSortIndex

                node.sort = newSort

                // 递归更新所有子节点的排序
                updateChildrenSort(node.key, newSort)
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
        pathNodeMap.remove(node.nodePath)

        // 从父子映射中删除
        val parentKey = node.parentKey
        parentChildMap[parentKey]!!.remove(node)
        if (parentChildMap[parentKey]!!.isEmpty()) {
            parentChildMap.remove(parentKey)
        }

        // 从父节点的子列表中删除
        val nodeParentKey = node.parentKey
        if (nodeParentKey != dummyKey) {
            val parentNode = findNodeByKey(nodeParentKey)
            parentNode!!.children.remove(node)
        }
    }

    /**
     * 获取排序索引（排序值的最后两位）
     */
    private fun getSortIndex(sort: Long): Long {
        return sort % sortBase
    }

    /**
     * 获取父节点排序值
     */
    private fun getParentSort(sort: Long): Long {
        return sort / sortBase
    }

    /**
     * 移动节点到新的父节点下
     */
    override fun moveNode(key: K, newParentKey: K, newSort: Long?): OrderedTreeNode<K, V>? {
        // 查找节点
        val node = findNodeByKey(key) ?: return null
        return moveNode(node, newParentKey, newSort)
    }

    /**
     * 移动节点到新的父节点下
     */
    override fun moveNode(node: OrderedTreeNode<K, V>, newParentKey: K, newSort: Long?): OrderedTreeNode<K, V> {
        // 检查参数
        require(node is DefaultOrderedTreeNode) { "Node must be an instance of DefaultOrderedTreeNode" }

        val nodeKey = node.key
        val oldParentKey = node.parentKey

        // 如果新旧父节点相同，且没有指定新的排序值，则无需移动
        if (oldParentKey == newParentKey && newSort == null) {
            return node
        }

        // 检查是否会导致循环依赖
        require(
            !wouldCreateCycle(
                nodeKey,
                newParentKey
            )
        ) { "Moving node $nodeKey to parent $newParentKey would create a cycle in the tree" }

        val originalParentKey = node.parentKey
        val originalSort = node.sort
        // 从原父节点的子节点列表中移除
        if (originalParentKey != dummyKey) {
            val parentNode = findNodeByKey(originalParentKey)
            parentNode?.children?.remove(node)
        }

        // 从父子映射中移除
        parentChildMap[originalParentKey]!!.remove(node)
        if (parentChildMap[originalParentKey]!!.isEmpty()) {
            parentChildMap.remove(originalParentKey)
        }

        // 从路径映射中移除
        pathNodeMap.remove(node.nodePath)

        // 计算新的排序值
        val actualNewSort = if (newSort != null) {
            // 如果指定了排序值，使用指定值
            val nextAvailableIndex = calculateNextSort(newParentKey)
            // 如果指定的排序值大于下一个可用的索引，使用下一个可用的索引
            if (newSort > nextAvailableIndex) nextAvailableIndex else newSort
        } else {
            // 否则自动计算下一个排序值
            calculateNextSort(newParentKey)
        }

        // 获取新父节点的排序值
        val newParentSort = if (newParentKey != dummyKey) findNodeByKey(newParentKey)?.sort ?: 0L else 0L

        // 计算实际的新排序值
        val actualSort = newParentSort * sortBase + actualNewSort

        // 更新节点的父键和排序值
        node.parentKey = newParentKey
        node.sort = actualSort

        // 生成新的节点路径
        val oldParentPath = findNodeByKey(originalParentKey)?.nodePath ?: ""
        val newParentPath = findNodeByKey(newParentKey)?.nodePath ?: ""
        val oldNodePath = node.nodePath
        node.nodePath = generateNodePath(nodeKey, newParentKey)

        // 更新所有子孙节点的路径和排序值
        updateDescendantsPathAndSort(
            originalSort.toString(), actualSort.toString(),
            oldNodePath,
            oldParentPath, newParentPath
        )

        // 处理可能的排序冲突
        handleSortConflict(newParentKey, actualSort)

        // 将节点添加到新父节点的子节点列表
        if (newParentKey != dummyKey) {
            val newParentNode = findNodeByKey(newParentKey)
            newParentNode?.children?.add(node)
        }

        // 更新映射
        parentChildMap.getOrPut(newParentKey) { mutableListOf() }.add(node)
        nodeMap[nodeKey] = node

        // 重新排序原父节点下的子节点
        val originalSortIndex = getSortIndex(originalSort)
        moveChildrenForward(originalParentKey, originalSortIndex)

        return node
    }

    /**
     * 批量移动节点到新的父节点下
     */
    override fun moveNodes(keys: Collection<K>, newParentKey: K): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()

        // 先检查所有节点是否存在
        val nodesToMove = keys.mapNotNull { findNodeByKey(it) }
        require(nodesToMove.size == keys.size) { "Some nodes do not exist" }

        // 检查是否会导致循环依赖
        for (node in nodesToMove) {
            require(
                !wouldCreateCycle(
                    node.key,
                    newParentKey
                )
            ) { "Moving node ${node.key} to parent $newParentKey would create a cycle in the tree" }
        }

        // 按排序值排序，确保按顺序移动
        val sortedNodes = nodesToMove.sortedBy { it.sort }

        // 移动每个节点
        for (node in sortedNodes) {
            val movedNode = moveNode(node, newParentKey)
            result.add(movedNode)
        }

        return result
    }

    /**
     * 通过节点路径移动节点
     */
    override fun moveNodeByPath(nodePath: String, newParentPath: String): OrderedTreeNode<K, V> {
        // 查找源节点和目标父节点
        val node = pathNodeMap[nodePath]
        val newParentNode = pathNodeMap[newParentPath]

        requireNotNull(node) { "Source node with path $nodePath not found" }
        requireNotNull(newParentNode) { "Target parent node with path $newParentPath not found" }

        // 移动节点
        return moveNode(node, newParentNode.key)
    }

    /**
     * 检查移动节点是否会导致循环依赖
     */
    private fun wouldCreateCycle(nodeKey: K, targetParentKey: K): Boolean {
        // 如果目标父节点就是当前节点，肯定会导致循环
        if (nodeKey == targetParentKey) {
            return true
        }

        // 检查目标父节点是否是当前节点的子孙节点
        var currentKey: K? = targetParentKey
        while (currentKey != null) {
            val currentNode = findNodeByKey(currentKey) ?: break
            if (currentNode.key == nodeKey) {
                return true
            }
            currentKey = currentNode.parentKey
        }

        return false
    }

    /**
     * 更新子孙节点的路径和排序值
     */
    private fun updateDescendantsPathAndSort(
        oldParentSortStr: String,
        newParentSortStr: String,
        odlNodePath: String,
        oldBasePath: String,
        newBasePath: String
    ) {
        // 批量更新所有子孙节点
        getDescendants(odlNodePath).forEach { descendant ->
            if (descendant is DefaultOrderedTreeNode) {
                // 1. 更新路径
                val oldPath = descendant.nodePath
                val newPath = oldPath.replace(oldBasePath, newBasePath)

                // 2. 更新排序值 - 使用字符串替换以保持相对排序关系
                val oldSortStr = descendant.sort.toString()
                val newSortStr = if (oldSortStr.startsWith(oldParentSortStr)) {
                    oldSortStr.replaceFirst(oldParentSortStr, newParentSortStr)
                } else throw IllegalArgumentException(
                    "Old sort string $oldSortStr does not start with expected parent sort $oldParentSortStr"
                )

                // 3. 从旧路径映射中移除
                pathNodeMap.remove(oldPath)

                // 4. 更新节点属性
                descendant.nodePath = newPath
                descendant.sort = newSortStr.toLong()

                // 5. 添加到新路径映射
                pathNodeMap[newPath] = descendant
            }
        }
    }
}
